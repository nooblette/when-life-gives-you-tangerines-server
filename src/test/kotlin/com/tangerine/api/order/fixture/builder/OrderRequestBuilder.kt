package com.tangerine.api.order.fixture.builder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tangerine.api.order.api.request.CustomerRequest
import com.tangerine.api.order.api.request.OrderItemRequest
import com.tangerine.api.order.api.request.PlaceOrderRequest

class OrderRequestBuilder {
    private val objectMapper: ObjectMapper = jacksonObjectMapper()

    // 기본 요청 데이터
    private val defaultCustomer: CustomerRequest =
        CustomerRequest(
            name = "홍길동",
            recipient = "이순신",
            phone = "010-1234-5678",
            address = "서울시 강남구 테헤란로 123",
            detailAddress = null,
            zipCode = "01234",
        )

    private val defaultItems =
        listOf(
            OrderItemRequest(
                id = 1L,
                name = "제주 노지 감귤",
                price = 12000,
                quantity = 2,
            ),
        )

    private var defaultTotalAmount =
        defaultItems.sumOf { (it.quantity.let { quantity -> it.price.times(quantity) }) }

    // 기본 Request 객체
    private var defaultRequest: PlaceOrderRequest =
        PlaceOrderRequest(
            customer = defaultCustomer,
            items = defaultItems,
            totalAmount = defaultTotalAmount,
        )

    fun withCustomerNameBlank() =
        apply {
            val defaultCustomerWithBlankName = defaultCustomer.copy(name = " ")
            defaultRequest = defaultRequest.copy(customer = defaultCustomerWithBlankName)
        }

    fun withOrderItemMinusPrice() =
        apply {
            val defaultItemWithMinusPrice = defaultItems[0].copy(price = defaultItems[0].price * -1)
            this.defaultRequest = defaultRequest.copy(items = listOf(defaultItemWithMinusPrice))
        }

    fun withOrderItemQuantityZero() =
        apply {
            val defaultItemWithQuantityZero = defaultItems[0].copy(quantity = 0)
            this.defaultRequest = defaultRequest.copy(items = listOf(defaultItemWithQuantityZero))
        }

    fun withEmptyItemList() = apply { defaultRequest = defaultRequest.copy(items = emptyList()) }

    fun withMinusTotalAmount() = apply { defaultRequest = defaultRequest.copy(totalAmount = -1) }

    // 최종 Json 문자열 생성
    fun build(): String = objectMapper.writeValueAsString(defaultRequest)
}

package com.tangerine.api.order.fixture

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tangerine.api.order.api.request.CustomerRequest
import com.tangerine.api.order.api.request.OrderItemRequest
import com.tangerine.api.order.api.request.OrderRequest

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
        defaultItems.sumOf { (it.quantity?.let { quantity -> it.price?.times(quantity) }) ?: -1 }

    private val customerFieldsToRemove = mutableSetOf<String>()
    private val itemFieldToRemovals = mutableMapOf<Int, MutableSet<String>>()

    // 기본 Request 객체
    private var defaultRequest: OrderRequest =
        OrderRequest(
            customer = defaultCustomer,
            items = defaultItems,
            totalAmount = defaultTotalAmount,
        )

    // 전체 필드 누락 메서드
    fun withoutCustomer() = apply { defaultRequest = defaultRequest.copy(customer = null) }

    fun withoutItems() = apply { defaultRequest = defaultRequest.copy(items = null) }

    fun withEmptyItemList() = apply { defaultRequest = defaultRequest.copy(items = emptyList()) }

    fun withoutTotalAmount() = apply { defaultRequest = defaultRequest.copy(totalAmount = null) }

    fun withMinusTotalAmount() = apply { defaultRequest = defaultRequest.copy(totalAmount = -1) }

    // 일부 필드 누락 메서드
    fun withoutCustomerField(fieldName: String) = apply { customerFieldsToRemove.add(fieldName) }

    fun withoutItemField(
        index: Int,
        fieldName: String,
    ) = apply {
        if (index < defaultItems.size) {
            itemFieldToRemovals.computeIfAbsent(index) { mutableSetOf() }.add(fieldName)
        }
    }

    // JSON 트리에서 특정 필드를 제거
    private fun removeFields(
        node: ObjectNode,
        fields: Collection<String>,
    ) {
        fields.forEach(node::remove)
    }

    // items 배열에서 itemId 기준으로 필드를 제거
    private fun removeItemFields(itemsArray: ArrayNode) {
        itemFieldToRemovals.forEach { (index, fields) ->
            val node = itemsArray.get(index)
            if (node is ObjectNode) {
                removeFields(node, fields)
            }
        }
    }

    // 최종 Json 문자열 생성
    fun build(): String {
        if (customerFieldsToRemove.isEmpty() && itemFieldToRemovals.isEmpty()) {
            return objectMapper.writeValueAsString(defaultRequest)
        }

        // 요청 파라미터를 JSON 트리 구조로 배핑
        val rootNode = objectMapper.valueToTree<ObjectNode>(defaultRequest)

        // customer 하위 필드 제거
        if (rootNode.has("customer") && customerFieldsToRemove.isNotEmpty()) {
            removeFields(node = rootNode.withObject("customer"), fields = customerFieldsToRemove)
        }

        // items 하위 필드 제거
        if (rootNode.has("items") && itemFieldToRemovals.isNotEmpty()) {
            removeItemFields(itemsArray = rootNode.withArray("items"))
        }

        return objectMapper.writeValueAsString(rootNode)
    }
}

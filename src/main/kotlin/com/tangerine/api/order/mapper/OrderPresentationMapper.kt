package com.tangerine.api.order.mapper

import com.tangerine.api.order.api.request.CustomerRequest
import com.tangerine.api.order.api.request.OrderItemRequest
import com.tangerine.api.order.api.request.OrderRequest
import com.tangerine.api.order.domain.Customer
import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.exception.MissingRequestFieldException

fun <T> T?.orMissing(fieldName: String): T = this ?: throw MissingRequestFieldException(fieldName)

fun OrderRequest.toDomain(): Order =
    Order(
        customer = customer.orMissing("주문 정보(customer)").toDomain(),
        items = items.orMissing("주문 상품(items)").map { it.orMissing("주문 상품 목록").toDomain() },
        totalAmount = totalAmount.orMissing("총 주문 금액(totalAmount)"),
    )

fun CustomerRequest.toDomain(): Customer =
    Customer(
        name = this.name.orMissing("주문자 이름(name)"),
        recipient = this.recipient.orMissing("받는 사람(recipient)"),
        phone = this.phone.orMissing("연락처(phone)"),
        address = this.address.orMissing("주소(address)"),
        detailAddress = this.detailAddress,
        zipCode = this.zipCode.orMissing("우편번호(zipCode)"),
    )

fun OrderItemRequest.toDomain(): OrderItem =
    OrderItem(
        id = this.id.orMissing("상품 Id"),
        name = this.name.orMissing("상품 이름(name)"),
        price = this.price.orMissing("상품 가격(price)"),
        quantity = this.quantity.orMissing("주문 수량(quantity)"),
    )

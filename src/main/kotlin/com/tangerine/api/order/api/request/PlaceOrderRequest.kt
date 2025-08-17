package com.tangerine.api.order.api.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty

data class PlaceOrderRequest(
    @field:Valid
    val customer: CustomerRequest,
    @field:Valid
    @field:NotEmpty(message = "주문상품(items)목록은 공백일 수 없습니다.")
    val items: List<OrderItemRequest>,
    @field:Min(0, message = "주문금액은 음수(-)일 수 없습니다.")
    val totalAmount: Int,
)

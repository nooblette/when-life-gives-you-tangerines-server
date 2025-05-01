package com.tangerine.api.order.api.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class OrderItemRequest(
    val id: Long,
    @field:NotBlank
    val name: String,
    @field:Min(0, message = "상품 가격은 음수(-)일 수 없습니다.")
    val price: Int,
    @field:Min(1, message = "상품 주문 수량은 0 이하일 수 없습니다.")
    val quantity: Int,
)

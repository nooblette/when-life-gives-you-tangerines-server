package com.tangerine.api.order.api.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
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

data class CustomerRequest(
    @field:NotBlank
    val name: String,
    @field:NotBlank
    val recipient: String,
    @field:NotBlank
    val phone: String,
    @field:NotBlank
    val address: String,
    val detailAddress: String?,
    @field:NotBlank
    val zipCode: String,
)

data class OrderItemRequest(
    val id: Long,
    @field:NotBlank
    val name: String,
    @field:Min(0, message = "상품 가격은 음수(-)일 수 없습니다.")
    val price: Int,
    @field:Min(1, message = "상품 주문 수량은 0 이하일 수 없습니다.")
    val quantity: Int,
)

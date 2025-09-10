package com.tangerine.api.order.api.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

data class PlaceOrderRequest(
    @field:Valid
    val customer: CustomerRequest,
    @field:Valid
    @field:NotEmpty(message = "주문상품(items)목록은 공백일 수 없습니다.")
    val items: List<OrderItemRequest>,
    @field:Min(0, message = "주문금액은 음수(-)일 수 없습니다.")
    val totalAmount: Int,
    @field:Valid
    val orderName: String,
)

data class CustomerRequest(
    @field:NotBlank
    @field:Size(max = 80, message = "이름은 80자를 초과할 수 없습니다.")
    val name: String,
    @field:NotBlank
    @field:Size(max = 80, message = "수령인은 80자를 초과할 수 없습니다.")
    val recipient: String,
    @field:NotBlank
    @field:Pattern(regexp = "^\\d{3}-\\d{4}-\\d{4}$", message = "전화번호는 010-xxxx-xxxx 형식이어야 합니다.")
    val phone: String,
    @field:NotBlank
    @field:Size(max = 80, message = "주소는 80자를 초과할 수 없습니다.")
    val address: String,
    @field:Size(max = 80, message = "상세주소는 80자를 초과할 수 없습니다.")
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

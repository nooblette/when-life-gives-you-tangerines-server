package com.tangerine.api.order.api.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.NotNull

data class OrderRequest(
    @field:Valid
    @field:NotNull(message = "주문정보(customer)는 필수값입니다.")
    val customer: CustomerRequest?,
    @field:Valid
    @field:NotNull(message = "주문상품(items)은 필수값입니다.")
    @field:NotEmpty(message = "주문상품(items)목록은 공백일 수 없습니다.")
    val items: List<OrderItemRequest?>?,
    @field:Min(0)
    @field:NotNull(message = "총 금액(totalAmount)은 필수값입니다.")
    val totalAmount: Int?,
)

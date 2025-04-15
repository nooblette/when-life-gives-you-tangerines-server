package com.tangerine.api.order.api.request

import jakarta.validation.Valid
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotNull

data class OrderRequest(
    @field:Valid
    val customer: CustomerRequest,
    @field:Valid
    val items: List<OrderItemRequest>,
    @field:NotNull @field:Min(0)
    val totalAmount: Int,
)

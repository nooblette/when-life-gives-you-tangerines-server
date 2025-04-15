package com.tangerine.api.order.api.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull

data class OrderItemRequest(
    @field:NotNull
    val id: Long,
    @field:NotBlank
    val name: String,
    @field:Min(0)
    val price: Int,
    @field:Min(1)
    val quantity: Int,
)

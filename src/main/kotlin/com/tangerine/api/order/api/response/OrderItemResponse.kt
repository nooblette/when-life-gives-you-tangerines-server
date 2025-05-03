package com.tangerine.api.order.api.response

data class OrderItemResponse(
    val id: Long,
    val name: String,
    val price: Int,
    val quantity: Int,
)

package com.tangerine.api.order.api.response

data class OrderResponse(
    val orderId: String,
    val customer: CustomerResponse,
    val items: List<OrderItemResponse>,
    val totalAmount: Int,
)

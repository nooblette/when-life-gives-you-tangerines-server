package com.tangerine.api.order.api.response

data class OrderResponse(
    val orderId: String,
    val customer: CustomerResponse,
    val items: List<OrderItemResponse>,
    val totalAmount: Int,
)

data class CustomerResponse(
    val name: String,
    val recipient: String,
    val phone: String,
    val address: String,
    val detailAddress: String?,
    val zipCode: String,
)

data class OrderItemResponse(
    val id: Long,
    val name: String,
    val price: Int,
    val quantity: Int,
)

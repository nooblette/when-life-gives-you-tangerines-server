package com.tangerine.api.order.event

data class OrderPaymentEvent(
    val orderId: String,
    val totalAmount: Int,
    val customerName: String,
    val customerPhone: String,
)

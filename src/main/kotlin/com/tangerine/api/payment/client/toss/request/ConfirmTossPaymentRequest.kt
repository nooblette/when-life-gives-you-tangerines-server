package com.tangerine.api.payment.client.toss.request

data class ConfirmTossPaymentRequest(
    val orderId: String,
    val amount: Int,
    val paymentKey: String,
)

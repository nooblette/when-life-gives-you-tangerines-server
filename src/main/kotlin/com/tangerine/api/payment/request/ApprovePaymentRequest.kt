package com.tangerine.api.payment.request

data class ApprovePaymentRequest(
    val orderId: String,
    val amount: Int,
    val paymentKey: String,
)

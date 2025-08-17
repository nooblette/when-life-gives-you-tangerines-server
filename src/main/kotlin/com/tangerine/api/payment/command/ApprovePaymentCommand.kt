package com.tangerine.api.payment.command

data class ApprovePaymentCommand(
    val orderId: String,
    val amount: Int,
    val paymentKey: String,
)

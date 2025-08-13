package com.tangerine.api.payment.command

data class PaymentApproveCommand(
    val orderId: String,
    val amount: Int,
    val paymentKey: String,
)

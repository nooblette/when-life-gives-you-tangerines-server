package com.tangerine.api.payment.result

import java.time.LocalDateTime

sealed class ApprovePaymentResult(
    open val paymentKey: String,
) {
    data class Success(
        override val paymentKey: String,
        val paymentMethod: String,
        val orderId: String,
        val orderName: String,
        val totalAmount: Int,
        val requestedAt: LocalDateTime,
        val approvedAt: LocalDateTime,
    ) : ApprovePaymentResult(paymentKey = paymentKey)

    data class Failure(
        override val paymentKey: String,
        val message: String,
        val code: String,
    ) : ApprovePaymentResult(paymentKey = paymentKey)
}

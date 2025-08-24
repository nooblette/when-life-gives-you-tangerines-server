package com.tangerine.api.order.result

import java.time.LocalDateTime

sealed class ApproveOrderPaymentResult(
    open val message: String,
) {
    data class Success(
        override val message: String = "Ok",
        val orderId: String,
        val orderName: String,
        val paymentKey: String,
        val paymentMethod: String,
        val totalAmount: Int,
        val requestedAt: LocalDateTime,
        val approvedAt: LocalDateTime,
    ) : ApproveOrderPaymentResult(message = message)

    data class Failure(
        override val message: String,
        val code: String,
    ) : ApproveOrderPaymentResult(message = message)
}

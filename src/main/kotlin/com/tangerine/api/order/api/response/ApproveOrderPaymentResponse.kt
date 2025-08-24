package com.tangerine.api.order.api.response

import java.time.LocalDateTime

sealed interface ApproveOrderPaymentResponse {
    data class Success(
        val orderId: String,
        val paymentKey: String,
        val totalAmount: Int,
        val requestedAt: LocalDateTime,
        val approvedAt: LocalDateTime,
        val paymentMethod: String,
        val message: String,
    ) : ApproveOrderPaymentResponse

    data class
    Failure(
        val code: String,
        val message: String,
    ) : ApproveOrderPaymentResponse
}

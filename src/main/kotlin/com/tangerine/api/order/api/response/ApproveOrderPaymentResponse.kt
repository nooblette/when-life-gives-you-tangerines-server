package com.tangerine.api.order.api.response

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
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

    data class Failure(
        val code: String,
        val message: String,
    ) : ApproveOrderPaymentResponse {
        fun toResponseEntity(): ResponseEntity<ApproveOrderPaymentResponse> =
            when (code) {
                "UNAUTHORIZED_KEY" -> ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(this)
                else -> ResponseEntity.badRequest().body(this)
            }
    }
}

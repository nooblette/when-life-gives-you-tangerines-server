package com.tangerine.api.payment.response

import com.tangerine.api.payment.domain.PaymentGateway
import java.time.LocalDateTime

sealed class ApprovePaymentResponse(
    open val paymentKey: String,
    open val paymentGateway: PaymentGateway,
) {
    data class Success(
        override val paymentKey: String,
        override val paymentGateway: PaymentGateway,
        val paymentMethod: String,
        val orderId: String,
        val orderName: String,
        val totalAmount: Int,
        val requestedAt: LocalDateTime,
        val approvedAt: LocalDateTime,
    ) : ApprovePaymentResponse(paymentKey = paymentKey, paymentGateway = paymentGateway)

    data class Failure(
        override val paymentKey: String,
        override val paymentGateway: PaymentGateway,
        val code: String,
        val message: String,
    ) : ApprovePaymentResponse(paymentKey = paymentKey, paymentGateway = paymentGateway) {
        companion object {
            fun apiCallError(
                paymentKey: String,
                paymentGateway: PaymentGateway,
                message: String?,
            ) = Failure(
                paymentKey = paymentKey,
                paymentGateway = paymentGateway,
                code = "API_CALL_ERROR",
                message = message ?: "알 수 없는 오류로 결제 요청에 실패했습니다.",
            )

            fun unknownError(
                paymentKey: String,
                paymentGateway: PaymentGateway,
            ) = Failure(
                paymentKey = paymentKey,
                paymentGateway = paymentGateway,
                code = "UNKNOWN_ERROR",
                message = "알 수 없는 오류로 결제가 실패했습니다.",
            )
        }
    }
}

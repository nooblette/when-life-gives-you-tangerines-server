package com.tangerine.api.payment.result

sealed class ApprovePaymentResult(
    open val paymentKey: String,
) {
    data class Success(
        override val paymentKey: String,
    ) : ApprovePaymentResult(paymentKey = paymentKey)

    data class Failure(
        override val paymentKey: String,
        val message: String,
        val code: String,
    ) : ApprovePaymentResult(paymentKey = paymentKey)
}

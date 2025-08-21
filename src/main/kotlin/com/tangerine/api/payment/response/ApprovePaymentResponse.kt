package com.tangerine.api.payment.response

sealed class ApprovePaymentResponse(
    open val paymentKey: String,
) {
    data class Success<T>(
        override val paymentKey: String,
        val data: T,
    ) : ApprovePaymentResponse(paymentKey = paymentKey)

    data class Failure(
        override val paymentKey: String,
        val code: String,
        val message: String,
    ) : ApprovePaymentResponse(paymentKey = paymentKey) {
        companion object {
            fun apiCallError(paymentKey: String) =
                Failure(
                    paymentKey = paymentKey,
                    code = "API_CALL_ERROR",
                    message = "알 수 없는 오류로 결제 요청에 실패했습니다.",
                )

            fun unknownError(paymentKey: String) =
                Failure(
                    paymentKey = paymentKey,
                    code = "UNKNOWN_ERROR",
                    message = "알 수 없는 오류로 결제가 실패했습니다.",
                )
        }
    }
}

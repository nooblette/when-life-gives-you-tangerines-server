package com.tangerine.api.order.result

sealed class ApproveOrderPaymentResult(
    open val message: String,
) {
    data class Success(
        override val message: String = "Ok",
    ) : ApproveOrderPaymentResult(message = message)

    data class Failure(
        override val message: String,
        val code: String,
    ) : ApproveOrderPaymentResult(message = message)
}

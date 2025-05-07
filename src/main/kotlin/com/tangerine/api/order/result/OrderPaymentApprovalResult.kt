package com.tangerine.api.order.result

sealed class OrderPaymentApprovalResult(
    open val message: String,
) {
    data class Success(
        override val message: String = "Ok",
    ) : OrderPaymentApprovalResult(message = message)

    data class Failure(
        override val message: String,
        val code: String,
    ) : OrderPaymentApprovalResult(message = message)
}

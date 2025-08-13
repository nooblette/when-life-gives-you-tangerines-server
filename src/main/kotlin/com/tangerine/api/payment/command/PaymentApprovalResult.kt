package com.tangerine.api.payment.command

sealed interface PaymentApprovalResult {
    data class Success(
        val paymentKey: String,
    ) : PaymentApprovalResult

    data class Failure(
        val paymentKey: String,
        val message: String,
        val code: String,
    ) : PaymentApprovalResult
}

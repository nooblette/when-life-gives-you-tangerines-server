package com.tangerine.api.payment.mapper

import com.tangerine.api.payment.response.ApprovePaymentResponse
import com.tangerine.api.payment.result.ApprovePaymentResult

fun ApprovePaymentResponse.toApprovePaymentResult(): ApprovePaymentResult =
    when (this) {
        is ApprovePaymentResponse.Success -> {
            ApprovePaymentResult.Success(paymentKey = this.paymentKey)
        }

        is ApprovePaymentResponse.Failure -> {
            ApprovePaymentResult.Failure(
                paymentKey = this.paymentKey,
                code = this.code,
                message = this.message,
            )
        }
    }

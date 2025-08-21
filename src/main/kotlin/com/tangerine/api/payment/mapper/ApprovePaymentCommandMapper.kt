package com.tangerine.api.payment.mapper

import com.tangerine.api.payment.command.ApprovePaymentCommand
import com.tangerine.api.payment.request.ApprovePaymentRequest

fun ApprovePaymentCommand.toApprovePaymentRequest() =
    ApprovePaymentRequest(
        orderId = this.orderId,
        amount = this.amount,
        paymentKey = this.paymentKey,
    )

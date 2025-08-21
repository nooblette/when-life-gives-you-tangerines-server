package com.tangerine.api.payment.mapper

import com.tangerine.api.payment.client.toss.request.ConfirmTossPaymentRequest
import com.tangerine.api.payment.request.ApprovePaymentRequest

fun ApprovePaymentRequest.toConfirmTossPaymentRequest() =
    ConfirmTossPaymentRequest(
        orderId = this.orderId,
        amount = this.amount,
        paymentKey = this.paymentKey,
    )

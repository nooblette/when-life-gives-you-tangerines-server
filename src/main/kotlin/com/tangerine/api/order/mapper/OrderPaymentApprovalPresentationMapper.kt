package com.tangerine.api.order.mapper

import com.tangerine.api.order.api.request.ApproveOrderPaymentRequest
import com.tangerine.api.order.command.ApproveOrderPaymentCommand

fun ApproveOrderPaymentRequest.toApproveOrderPaymentCommand(orderId: String) =
    ApproveOrderPaymentCommand(
        orderId = orderId,
        paymentKey = this.paymentKey,
        totalAmount = this.totalAmount,
    )

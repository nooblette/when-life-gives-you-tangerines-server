package com.tangerine.api.order.mapper

import com.tangerine.api.order.api.request.OrderPaymentApprovalRequest
import com.tangerine.api.order.command.ApproveOrderPaymentCommand

fun OrderPaymentApprovalRequest.toCommand(orderId: String) =
    ApproveOrderPaymentCommand(
        orderId = orderId,
        paymentKey = this.paymentKey,
        totalAmount = this.totalAmount,
    )

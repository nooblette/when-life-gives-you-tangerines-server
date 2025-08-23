package com.tangerine.api.order.mapper

import com.tangerine.api.order.command.ApproveOrderPaymentCommand
import com.tangerine.api.payment.command.ApprovePaymentCommand

fun ApproveOrderPaymentCommand.toApprovePaymentCommand(): ApprovePaymentCommand =
    ApprovePaymentCommand(
        orderId = orderId,
        paymentKey = paymentKey,
        amount = totalAmount,
    )

package com.tangerine.api.payment.fixture.command

import com.tangerine.api.order.service.command.ApproveOrderPaymentCommand
import com.tangerine.api.payment.command.PaymentApproveCommand

fun PaymentApproveCommand.equals(orderPaymentCommand: ApproveOrderPaymentCommand) =
    this.orderId == orderPaymentCommand.orderId &&
        this.paymentKey == orderPaymentCommand.paymentKey &&
        this.amount == orderPaymentCommand.totalAmount

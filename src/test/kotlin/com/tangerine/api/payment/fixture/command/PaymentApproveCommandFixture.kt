package com.tangerine.api.payment.fixture.command

import com.tangerine.api.order.command.ApproveOrderPaymentCommand
import com.tangerine.api.payment.command.ApprovePaymentCommand

fun ApprovePaymentCommand.equals(orderPaymentCommand: ApproveOrderPaymentCommand) =
    this.orderId == orderPaymentCommand.orderId &&
        this.paymentKey == orderPaymentCommand.paymentKey &&
        this.amount == orderPaymentCommand.totalAmount

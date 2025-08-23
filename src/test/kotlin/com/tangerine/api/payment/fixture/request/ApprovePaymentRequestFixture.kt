package com.tangerine.api.payment.fixture.request

import com.tangerine.api.order.command.ApproveOrderPaymentCommand
import com.tangerine.api.payment.request.ApprovePaymentRequest

fun ApprovePaymentRequest.matchesOrderCommand(orderPaymentCommand: ApproveOrderPaymentCommand) =
    this.orderId == orderPaymentCommand.orderId &&
        this.paymentKey == orderPaymentCommand.paymentKey &&
        this.amount == orderPaymentCommand.totalAmount

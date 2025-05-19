package com.tangerine.api.order.fixture.domain

import com.tangerine.api.order.fixture.generator.TestOrderIdGenerator
import com.tangerine.api.order.service.command.ApproveOrderPaymentCommand

fun createApproveOrderPaymentCommand(totalAmount: Int = 10000): ApproveOrderPaymentCommand =
    ApproveOrderPaymentCommand(
        orderId = TestOrderIdGenerator.STUB_ORDER_ID,
        paymentKey = "TEST",
        totalAmount = totalAmount,
    )

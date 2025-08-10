package com.tangerine.api.order.fixture.domain

import com.tangerine.api.order.fixture.generator.TestOrderIdGenerator
import com.tangerine.api.order.service.command.ApproveOrderPaymentCommand

fun createApproveOrderPaymentCommand(
    orderId: String = TestOrderIdGenerator.STUB_ORDER_ID,
    totalAmount: Int = 10000,
): ApproveOrderPaymentCommand =
    ApproveOrderPaymentCommand(
        orderId = orderId,
        paymentKey = "TEST",
        totalAmount = totalAmount,
    )

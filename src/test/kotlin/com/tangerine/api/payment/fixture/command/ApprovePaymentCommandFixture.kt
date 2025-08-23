package com.tangerine.api.payment.fixture.command

import com.tangerine.api.payment.command.ApprovePaymentCommand
import java.util.concurrent.atomic.AtomicLong

private val orderSeq = AtomicLong(1)

fun createApprovePaymentCommand(): ApprovePaymentCommand =
    ApprovePaymentCommand(
        orderId = "PAYMENT_APPROVE_TEST_ID_${orderSeq.getAndIncrement()}",
        amount = 10_000,
        paymentKey = "DUMMY_KEY",
    )

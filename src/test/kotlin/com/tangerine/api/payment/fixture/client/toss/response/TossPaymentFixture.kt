package com.tangerine.api.payment.fixture.client.toss.response

import com.tangerine.api.payment.client.toss.response.PaymentStatus
import com.tangerine.api.payment.client.toss.response.PaymentType
import com.tangerine.api.payment.client.toss.response.TossPayment
import java.time.LocalDateTime

fun tossPayment() =
    TossPayment(
        version = "2022-07-27",
        paymentKey = "5zJ4xY7m0kODnyRpQWGrN2xqGlNvLrKwv1M9ENjbeoPaZdL6",
        type = PaymentType.NORMAL,
        orderId = "ORDER_20240821_001",
        orderName = "티셔츠 외 2건",
        mId = "tosspayments",
        currency = "KRW",
        method = null,
        totalAmount = 50000,
        balanceAmount = 50000,
        status = PaymentStatus.DONE,
        requestedAt = LocalDateTime.of(2024, 8, 21, 14, 30, 0),
        approvedAt = LocalDateTime.of(2024, 8, 21, 14, 30, 10),
        useEscrow = false,
        lastTransactionKey = null,
        suppliedAmount = 45455,
        vat = 4545,
        cultureExpense = false,
        taxFreeAmount = 0,
        taxExemptionAmount = 0,
        isPartialCancelable = true,
        cancels = null,
        card = null,
        virtualAccount = null,
        secret = null,
        mobilePhone = null,
        giftCertificate = null,
        transfer = null,
        metadata = null,
        receipt = null,
        checkout = null,
        easyPay = null,
        country = "KR",
        failure = null,
        cashReceipt = null,
        cashReceipts = null,
        discount = null,
    )

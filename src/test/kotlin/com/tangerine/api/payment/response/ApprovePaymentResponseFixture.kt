package com.tangerine.api.payment.response

import java.time.LocalDateTime

fun success(paymentKey: String): ApprovePaymentResponse =
    ApprovePaymentResponse.Success(
        paymentKey = paymentKey,
        orderName = "제주 감귤 10kg 1개 외 2건",
        requestAt = LocalDateTime.now(),
        approvedAt = LocalDateTime.now(),
    )

package com.tangerine.api.payment.response

import java.time.LocalDateTime

fun success(paymentKey: String): ApprovePaymentResponse.Success =
    ApprovePaymentResponse.Success(
        paymentKey = paymentKey,
        orderName = "제주 감귤 10kg 1개 외 2건",
        requestAt = LocalDateTime.now(),
        approvedAt = LocalDateTime.now(),
    )

fun failure(paymentKey: String): ApprovePaymentResponse.Failure =
    ApprovePaymentResponse.Failure(
        paymentKey = paymentKey,
        code = "PAYMENT_FAIL_TEST",
        message = "결제 승인 요청에 실패했습니다.",
    )

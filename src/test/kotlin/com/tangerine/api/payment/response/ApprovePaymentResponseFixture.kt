package com.tangerine.api.payment.response

import com.tangerine.api.payment.domain.PaymentGateway.TOSS
import java.time.LocalDateTime

fun success(
    orderId: String,
    paymentKey: String,
): ApprovePaymentResponse.Success =
    ApprovePaymentResponse.Success(
        paymentKey = paymentKey,
        paymentGateway = TOSS,
        paymentMethod = "CARD",
        orderId = orderId,
        orderName = "제주 감귤 10kg 1개 외 2건",
        totalAmount = 32000,
        requestedAt = LocalDateTime.now(),
        approvedAt = LocalDateTime.now(),
    )

fun failure(paymentKey: String): ApprovePaymentResponse.Failure =
    ApprovePaymentResponse.Failure(
        paymentKey = paymentKey,
        paymentGateway = TOSS,
        code = "PAYMENT_FAIL_TEST",
        message = "결제 승인 요청에 실패했습니다.",
    )

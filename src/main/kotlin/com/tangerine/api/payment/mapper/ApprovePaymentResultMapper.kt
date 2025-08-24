package com.tangerine.api.payment.mapper

import com.tangerine.api.order.result.ApproveOrderPaymentResult
import com.tangerine.api.payment.result.ApprovePaymentResult

fun ApprovePaymentResult.toApproveOrderPaymentResult(): ApproveOrderPaymentResult =
    when (this) {
        is ApprovePaymentResult.Success -> {
            ApproveOrderPaymentResult.Success(
                orderId = this.orderId,
                orderName = this.orderName,
                paymentKey = this.paymentKey,
                paymentMethod = this.paymentMethod,
                totalAmount = this.totalAmount,
                requestedAt = this.requestedAt,
                approvedAt = this.approvedAt,
            )
        }

        is ApprovePaymentResult.Failure -> {
            ApproveOrderPaymentResult.Failure(
                message = this.message,
                code = this.code,
            )
        }
    }

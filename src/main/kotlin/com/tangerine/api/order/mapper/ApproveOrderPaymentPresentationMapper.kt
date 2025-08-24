package com.tangerine.api.order.mapper

import com.tangerine.api.order.api.request.ApproveOrderPaymentRequest
import com.tangerine.api.order.api.response.ApproveOrderPaymentResponse
import com.tangerine.api.order.command.ApproveOrderPaymentCommand
import com.tangerine.api.order.result.ApproveOrderPaymentResult

fun ApproveOrderPaymentRequest.toApproveOrderPaymentCommand(orderId: String) =
    ApproveOrderPaymentCommand(
        orderId = orderId,
        paymentKey = this.paymentKey,
        totalAmount = this.totalAmount,
    )

fun ApproveOrderPaymentResult.toResponse(): ApproveOrderPaymentResponse =
    when (this) {
        is ApproveOrderPaymentResult.Success -> {
            ApproveOrderPaymentResponse.Success(
                orderId = this.orderId,
                paymentKey = this.paymentKey,
                totalAmount = this.totalAmount,
                paymentMethod = this.paymentMethod,
                requestedAt = this.requestedAt,
                approvedAt = this.approvedAt,
                message = this.message,
            )
        }

        is ApproveOrderPaymentResult.Failure -> {
            ApproveOrderPaymentResponse.Failure(
                code = this.code,
                message = this.message,
            )
        }
    }

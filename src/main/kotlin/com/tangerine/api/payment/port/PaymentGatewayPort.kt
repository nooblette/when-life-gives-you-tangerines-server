package com.tangerine.api.payment.port

import com.tangerine.api.payment.request.ApprovePaymentRequest
import com.tangerine.api.payment.response.ApprovePaymentResponse

interface PaymentGatewayPort {
    fun approve(request: ApprovePaymentRequest): ApprovePaymentResponse
}

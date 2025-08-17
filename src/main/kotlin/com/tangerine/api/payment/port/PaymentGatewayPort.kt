package com.tangerine.api.payment.port

import com.tangerine.api.payment.command.ApprovePaymentCommand
import com.tangerine.api.payment.result.ApprovePaymentResult

interface PaymentGatewayPort {
    fun approve(command: ApprovePaymentCommand): ApprovePaymentResult
}

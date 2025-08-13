package com.tangerine.api.payment.port

import com.tangerine.api.payment.command.PaymentApprovalResult
import com.tangerine.api.payment.command.PaymentApproveCommand

interface PaymentGatewayPort {
    fun approve(command: PaymentApproveCommand): PaymentApprovalResult
}

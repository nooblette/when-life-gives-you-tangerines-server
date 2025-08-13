package com.tangerine.api.payment.port

import com.tangerine.api.payment.command.PaymentApprovalResult
import com.tangerine.api.payment.command.PaymentApproveCommand
import org.springframework.stereotype.Component

@Component
class TossPaymentGatewayAdaptor : PaymentGatewayPort {
    override fun approve(command: PaymentApproveCommand): PaymentApprovalResult {
        TODO("Not yet implemented")
    }
}

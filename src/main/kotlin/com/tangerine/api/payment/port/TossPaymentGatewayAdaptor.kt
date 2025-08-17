package com.tangerine.api.payment.port

import com.tangerine.api.payment.command.ApprovePaymentCommand
import com.tangerine.api.payment.result.ApprovePaymentResult
import org.springframework.stereotype.Component

@Component
class TossPaymentGatewayAdaptor : PaymentGatewayPort {
    override fun approve(command: ApprovePaymentCommand): ApprovePaymentResult {
        TODO("Not yet implemented")
    }
}

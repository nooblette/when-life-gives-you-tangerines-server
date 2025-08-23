package com.tangerine.api.payment.service

import com.tangerine.api.payment.domain.PaymentStatus
import com.tangerine.api.payment.entity.PaymentEntity
import com.tangerine.api.payment.response.ApprovePaymentResponse
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class PaymentStateService {
    @Transactional
    fun changeToCompleted(
        paymentEntity: PaymentEntity,
        paymentResponse: ApprovePaymentResponse.Success,
    ) {
        paymentEntity.status = PaymentStatus.COMPLETED
        paymentEntity.paymentGateway = paymentResponse.paymentGateway
        paymentEntity.orderName = paymentResponse.orderName
        paymentEntity.requestedAt = paymentResponse.requestedAt
        paymentEntity.approvedAt = paymentResponse.approvedAt
        logger.info { "Payment(id = ${paymentEntity.id}) 결제 상태 변경 (status = ${paymentEntity.status})" }
    }

    @Transactional
    fun changeToFailed(
        paymentEntity: PaymentEntity,
        paymentResponse: ApprovePaymentResponse.Failure,
    ) {
        paymentEntity.status = PaymentStatus.FAILED
        paymentEntity.paymentGateway = paymentResponse.paymentGateway
        paymentEntity.failCode = paymentResponse.code
        paymentEntity.failReason = paymentResponse.message
        logger.info { "Payment(id = ${paymentEntity.id}) 결제 상태 변경 (status = ${paymentEntity.status}), 사유: $paymentEntity.message" }
    }
}

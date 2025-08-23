package com.tangerine.api.payment.service

import com.tangerine.api.payment.domain.PaymentStatus
import com.tangerine.api.payment.entity.PaymentEntity
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

@Service
class PaymentStateService {
    @Transactional
    fun changeToCompleted(
        paymentEntity: PaymentEntity,
        orderName: String,
        requestAt: LocalDateTime,
        approvedAt: LocalDateTime,
    ) {
        paymentEntity.status = PaymentStatus.COMPLETED
        paymentEntity.orderName = orderName
        paymentEntity.requestedAt = requestAt
        paymentEntity.approvedAt = approvedAt
        logger.info { "결제 상태 ${PaymentStatus.COMPLETED}로 변경" }
    }

    @Transactional
    fun changeToFailed(
        paymentEntity: PaymentEntity,
        failCode: String,
        failReason: String,
    ) {
        paymentEntity.status = PaymentStatus.FAILED
        paymentEntity.failCode = failCode
        paymentEntity.failReason = failReason
        logger.info { "결제 상태 ${PaymentStatus.FAILED}로 변경, 실패 사유: $failReason" }
    }
}

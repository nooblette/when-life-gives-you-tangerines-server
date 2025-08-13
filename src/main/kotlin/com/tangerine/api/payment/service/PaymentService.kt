package com.tangerine.api.payment.service

import com.tangerine.api.payment.command.PaymentApproveCommand
import com.tangerine.api.payment.domain.PaymentStatus
import com.tangerine.api.payment.entity.PaymentEntity
import com.tangerine.api.payment.repository.PaymentRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
@Transactional(readOnly = true)
class PaymentService(
    private val paymentRepository: PaymentRepository,
) {
    @Transactional
    fun createPreparedPayment(command: PaymentApproveCommand): PaymentEntity =
        paymentRepository.save(
            PaymentEntity(
                orderId = command.orderId,
                amount = command.amount,
                paymentKey = command.paymentKey,
                status = PaymentStatus.PREPARED,
            ),
        )
}

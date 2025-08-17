package com.tangerine.api.payment.service

import com.tangerine.api.payment.command.ApprovePaymentCommand
import com.tangerine.api.payment.domain.PaymentStatus
import com.tangerine.api.payment.entity.PaymentEntity
import com.tangerine.api.payment.port.PaymentGatewayPort
import com.tangerine.api.payment.repository.PaymentRepository
import com.tangerine.api.payment.result.ApprovePaymentResult
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class PaymentService(
    private val paymentRepository: PaymentRepository,
    private val paymentGatewayPort: PaymentGatewayPort,
    private val paymentStateService: PaymentStateService,
) {
    @Transactional
    fun approvePayment(command: ApprovePaymentCommand): ApprovePaymentResult {
        logger.info { "결제 요청 시작 : $command" }

        val paymentEntity = createPreparedPayment(command)
        val paymentResult = paymentGatewayPort.approve(command)

        when (paymentResult) {
            is ApprovePaymentResult.Success -> {
                paymentStateService.changeToCompleted(paymentEntity)
                logger.info { "$command 결제 성공" }
            }

            is ApprovePaymentResult.Failure -> {
                paymentStateService.changeToFailed(paymentEntity, paymentResult.message)
                logger.info { "$command 결제 실패" }
            }
        }

        return paymentResult
    }

    private fun createPreparedPayment(command: ApprovePaymentCommand): PaymentEntity =
        paymentRepository.save(
            PaymentEntity(
                orderId = command.orderId,
                amount = command.amount,
                paymentKey = command.paymentKey,
                status = PaymentStatus.PREPARED,
            ),
        )
}

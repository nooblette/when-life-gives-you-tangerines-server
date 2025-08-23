package com.tangerine.api.payment.service

import com.tangerine.api.payment.command.ApprovePaymentCommand
import com.tangerine.api.payment.domain.PaymentStatus
import com.tangerine.api.payment.entity.PaymentEntity
import com.tangerine.api.payment.mapper.toApprovePaymentRequest
import com.tangerine.api.payment.mapper.toApprovePaymentResult
import com.tangerine.api.payment.port.PaymentGatewayPort
import com.tangerine.api.payment.repository.PaymentRepository
import com.tangerine.api.payment.response.ApprovePaymentResponse
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

        // 결제 승인 API 호출
        return paymentGatewayPort
            .approve(command.toApprovePaymentRequest())
            .also { paymentResponse ->
                when (paymentResponse) {
                    is ApprovePaymentResponse.Success -> successHandler(paymentEntity, paymentResponse, command)
                    is ApprovePaymentResponse.Failure -> failureHandler(paymentEntity, paymentResponse, command)
                }
            }.toApprovePaymentResult()
    }

    private fun successHandler(
        paymentEntity: PaymentEntity,
        paymentResponse: ApprovePaymentResponse.Success,
        command: ApprovePaymentCommand,
    ) {
        paymentStateService.changeToCompleted(
            paymentEntity = paymentEntity,
            orderName = paymentResponse.orderName,
            requestAt = paymentResponse.requestAt,
            approvedAt = paymentResponse.approvedAt,
        )
        logger.info { "$command 결제 성공" }
    }

    private fun failureHandler(
        paymentEntity: PaymentEntity,
        paymentResponse: ApprovePaymentResponse.Failure,
        command: ApprovePaymentCommand,
    ) {
        paymentStateService.changeToFailed(
            paymentEntity = paymentEntity,
            failCode = paymentResponse.code,
            failReason = paymentResponse.message,
        )
        logger.info { "$command 결제 실패" }
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

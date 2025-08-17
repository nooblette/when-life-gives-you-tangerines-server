package com.tangerine.api.order.usecase

import com.tangerine.api.order.command.ApproveOrderPaymentCommand
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.policy.OrderPaymentApprovalPolicy
import com.tangerine.api.order.result.ApproveOrderPaymentResult
import com.tangerine.api.order.result.EvaluateOrderPaymentResult
import com.tangerine.api.order.service.OrderQueryService
import com.tangerine.api.order.service.OrderStateService
import com.tangerine.api.payment.command.ApprovePaymentCommand
import com.tangerine.api.payment.result.ApprovePaymentResult
import com.tangerine.api.payment.service.PaymentService
import mu.KotlinLogging
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class ApproveOrderPaymentUseCase(
    private val orderQueryService: OrderQueryService,
    private val orderStateService: OrderStateService,
    private val paymentService: PaymentService,
    private val approvalPolicy: OrderPaymentApprovalPolicy,
) {
    @Transactional
    fun approve(command: ApproveOrderPaymentCommand): ApproveOrderPaymentResult {
        logger.info { "주문 승인 프로세스 시작 : $command" }

        // 주문 조회
        val order = orderQueryService.getOrderByOrderIdWithEntity(orderId = command.orderId)

        // 주문 결제 가능 여부 검증
        val evaluationResult =
            approvalPolicy.evaluate(
                order = order.domain,
                totalAmountForPayment = command.totalAmount,
            )

        return when (evaluationResult) {
            is EvaluateOrderPaymentResult.Failure -> evaluationResult.toApproveOrderPaymentResult()
            is EvaluateOrderPaymentResult.Success ->
                approveOrderPayment(
                    orderEntity = order.entity,
                    command = command,
                )
        }
    }

    private fun approveOrderPayment(
        orderEntity: OrderEntity,
        command: ApproveOrderPaymentCommand,
    ): ApproveOrderPaymentResult {
        // 주문 상태 변경 (IN_PROGRESS) 및 동시성 제어
        orderStateService.markAsInProgress(orderEntity = orderEntity)

        // 결제 처리 및 주문 상태 변경
        val paymentResult = paymentService.approvePayment(command = command.toApprovePaymentCommand())
        return paymentResult.toApproveOrderPaymentResult(
            orderEntity = orderEntity,
            command = command,
        )
    }

    private fun ApprovePaymentResult.toApproveOrderPaymentResult(
        orderEntity: OrderEntity,
        command: ApproveOrderPaymentCommand,
    ): ApproveOrderPaymentResult =
        when (this) {
            is ApprovePaymentResult.Success -> {
                orderStateService.changeToDone(orderEntity = orderEntity)
                logger.info { "$command 결제 성공, 주문 완료" }
                ApproveOrderPaymentResult.Success()
            }

            is ApprovePaymentResult.Failure -> {
                orderStateService.changeToPaymentFailure(orderEntity = orderEntity)
                logger.info { "$command 결제 실패" }
                ApproveOrderPaymentResult.Failure(
                    message = this.message,
                    code = this.code,
                )
            }
        }

    private fun EvaluateOrderPaymentResult.toApproveOrderPaymentResult(): ApproveOrderPaymentResult =
        when (this) {
            is EvaluateOrderPaymentResult.Failure -> ApproveOrderPaymentResult.Failure(message = message, code = code)
            is EvaluateOrderPaymentResult.Success -> ApproveOrderPaymentResult.Success()
        }

    private fun ApproveOrderPaymentCommand.toApprovePaymentCommand(): ApprovePaymentCommand =
        ApprovePaymentCommand(
            orderId = orderId,
            paymentKey = paymentKey,
            amount = totalAmount,
        )
}

package com.tangerine.api.order.usecase

import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.policy.OrderPaymentApprovalPolicy
import com.tangerine.api.order.result.OrderPaymentApprovalResult
import com.tangerine.api.order.result.OrderPaymentEvaluationResult
import com.tangerine.api.order.service.OrderQueryService
import com.tangerine.api.order.service.OrderStateService
import com.tangerine.api.order.usecase.command.ApproveOrderPaymentCommand
import com.tangerine.api.payment.command.PaymentApprovalResult
import com.tangerine.api.payment.command.PaymentApproveCommand
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
    fun approve(command: ApproveOrderPaymentCommand): OrderPaymentApprovalResult {
        logger.info { "주문 승인 프로세스 시작 : $command" }

        // 주문 조회
        val order = orderQueryService.getOrderByOrderIdWithEntity(orderId = command.orderId)

        // 주문 결제 가능 여부 검증
        val evaluationResult =
            approvalPolicy.evaluate(
                order = order.domain,
                totalAmountForPayment = command.totalAmount,
            )

        if (evaluationResult is OrderPaymentEvaluationResult.Failure) {
            return evaluationResult.toResult()
        }

        // 주문 상태 변경 (IN_PROGRESS) 및 동시성 제어
        orderStateService.markAsInProgress(order.entity)

        // 결제 처리 및 주문 상태 변경
        val paymentResult = paymentService.approvePayment(command.toPaymentApproveCommand())
        return paymentResult.toOrderPaymentApprovalResult(
            orderEntity = order.entity,
            command = command,
        )
    }

    private fun PaymentApprovalResult.toOrderPaymentApprovalResult(
        orderEntity: OrderEntity,
        command: ApproveOrderPaymentCommand,
    ): OrderPaymentApprovalResult =
        when (this) {
            is PaymentApprovalResult.Success -> {
                orderStateService.changeToDone(orderEntity)
                logger.info { "$command 결제 성공, 주문 완료" }
                OrderPaymentApprovalResult.Success()
            }

            is PaymentApprovalResult.Failure -> {
                orderStateService.changeToPaymentFailure(orderEntity)
                logger.info { "$command 결제 실패" }
                OrderPaymentApprovalResult.Failure(
                    message = this.message,
                    code = this.code,
                )
            }
        }

    private fun OrderPaymentEvaluationResult.toResult(): OrderPaymentApprovalResult =
        when (this) {
            is OrderPaymentEvaluationResult.Failure -> OrderPaymentApprovalResult.Failure(message, code)
            is OrderPaymentEvaluationResult.Success -> OrderPaymentApprovalResult.Success()
        }

    private fun ApproveOrderPaymentCommand.toPaymentApproveCommand(): PaymentApproveCommand =
        PaymentApproveCommand(
            orderId = orderId,
            paymentKey = paymentKey,
            amount = totalAmount,
        )
}

package com.tangerine.api.order.usecase

import com.tangerine.api.order.command.ApproveOrderPaymentCommand
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.mapper.toApprovePaymentCommand
import com.tangerine.api.order.policy.OrderPaymentApprovalPolicy
import com.tangerine.api.order.result.ApproveOrderPaymentResult
import com.tangerine.api.order.result.EvaluateOrderPaymentResult
import com.tangerine.api.order.service.OrderQueryService
import com.tangerine.api.order.service.OrderStateService
import com.tangerine.api.payment.mapper.toApproveOrderPaymentResult
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
            is EvaluateOrderPaymentResult.Success ->
                approveOrderPayment(
                    orderEntity = order.entity,
                    command = command,
                )

            is EvaluateOrderPaymentResult.Failure ->
                ApproveOrderPaymentResult.Failure(
                    message = evaluationResult.message,
                    code = evaluationResult.code,
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
        return paymentService
            .approvePayment(command.toApprovePaymentCommand())
            .also { approvePaymentResult ->
                when (approvePaymentResult) {
                    is ApprovePaymentResult.Success -> successHandler(orderEntity, command)
                    is ApprovePaymentResult.Failure -> failureHandler(orderEntity, command)
                }
            }.toApproveOrderPaymentResult()
    }

    private fun successHandler(
        orderEntity: OrderEntity,
        command: ApproveOrderPaymentCommand,
    ) {
        orderStateService.changeToDone(orderEntity = orderEntity)
        logger.info { "$command 결제 성공, 주문 완료" }
    }

    private fun failureHandler(
        orderEntity: OrderEntity,
        command: ApproveOrderPaymentCommand,
    ) {
        orderStateService.changeToPaymentFailure(orderEntity = orderEntity)
        logger.info { "$command 결제 실패" }
    }
}

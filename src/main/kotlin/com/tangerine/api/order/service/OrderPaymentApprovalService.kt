package com.tangerine.api.order.service

import com.tangerine.api.order.mapper.toPaymentEvent
import com.tangerine.api.order.policy.OrderPaymentApprovalPolicy
import com.tangerine.api.order.result.OrderPaymentApprovalResult
import com.tangerine.api.order.result.OrderPaymentEvaluationResult
import com.tangerine.api.order.service.command.ApproveOrderPaymentCommand
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderPaymentApprovalService(
    private val orderQueryService: OrderQueryService,
    private val orderCommandService: OrderCommandService,
    private val approvalPolicy: OrderPaymentApprovalPolicy,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun approve(approveOrderPaymentCommand: ApproveOrderPaymentCommand): OrderPaymentApprovalResult {
        val orderForPayment = orderQueryService.getOrderById(orderId = approveOrderPaymentCommand.orderId)
        val evaluationResult =
            approvalPolicy.evaluate(
                order = orderForPayment,
                totalAmountForPayment = approveOrderPaymentCommand.totalAmount,
            )

        return when (evaluationResult) {
            is OrderPaymentEvaluationResult.Failure -> {
                evaluationResult.toResult()
            }

            is OrderPaymentEvaluationResult.Success -> {
                // 주문 상태 변경(초기 -> 진행중)
                val orderInProgress = orderForPayment.markAsInProgress()
                orderCommandService.update(orderInProgress)

                // 결제 이벤트 발행
                eventPublisher.publishEvent(orderInProgress.toPaymentEvent())
                evaluationResult.toResult()
            }
        }
    }

    private fun OrderPaymentEvaluationResult.toResult(): OrderPaymentApprovalResult =
        when (this) {
            is OrderPaymentEvaluationResult.Failure -> OrderPaymentApprovalResult.Failure(message, code)
            is OrderPaymentEvaluationResult.Success -> OrderPaymentApprovalResult.Success()
        }
}

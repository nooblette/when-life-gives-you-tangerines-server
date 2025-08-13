package com.tangerine.api.order.service

import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.exception.OrderAlreadyInProgressException
import com.tangerine.api.order.mapper.toDomain
import com.tangerine.api.order.mapper.toDomains
import com.tangerine.api.order.policy.OrderPaymentApprovalPolicy
import com.tangerine.api.order.repository.OrderItemRepository
import com.tangerine.api.order.repository.OrderRepository
import com.tangerine.api.order.result.OrderPaymentApprovalResult
import com.tangerine.api.order.result.OrderPaymentEvaluationResult
import com.tangerine.api.order.service.command.ApproveOrderPaymentCommand
import com.tangerine.api.payment.command.PaymentApprovalResult
import com.tangerine.api.payment.command.PaymentApproveCommand
import com.tangerine.api.payment.domain.PaymentStatus
import com.tangerine.api.payment.port.PaymentGatewayPort
import com.tangerine.api.payment.service.PaymentService
import mu.KotlinLogging
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class OrderPaymentApprovalService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val approvalPolicy: OrderPaymentApprovalPolicy,
    private val paymentService: PaymentService,
    private val paymentGatewayPort: PaymentGatewayPort,
) {
    @Transactional
    fun approve(command: ApproveOrderPaymentCommand): OrderPaymentApprovalResult {
        try {
            logger.info { "주문 승인 프로세스 시작 : $command" }
            val orderEntity =
                requireNotNull(orderRepository.findByOrderId(command.orderId)) {
                    "주문을 찾을 수 없습니다."
                }

            val orderItems = orderItemRepository.findByOrder(orderEntity).toDomains()
            val orderForPayment = orderEntity.toDomain(orderItems)

            val evaluationResult =
                approvalPolicy.evaluate(
                    order = orderForPayment,
                    totalAmountForPayment = command.totalAmount,
                )

            if (evaluationResult is OrderPaymentEvaluationResult.Failure) {
                return evaluationResult.toResult()
            }

            // 주문 상태 변경 및 즉시 DB 반영
            orderEntity.status = OrderStatus.IN_PROGRESS

            // 불필요한 결제 진행 방지, 비즈니스 로직에서 버전 충돌을 빠르게 감지한다.
            logger.info { "주문 상태 ${OrderStatus.IN_PROGRESS}로 변경" }
            orderRepository.flush()

            // 주문 결제
            val paymentApproveCommand = command.toPaymentApproveCommand()
            logger.info { "결제 요청 시작 : $paymentApproveCommand" }
            val paymentEntity = paymentService.createPreparedPayment(paymentApproveCommand)
            val paymentResult = paymentGatewayPort.approve(paymentApproveCommand)
            when (paymentResult) {
                is PaymentApprovalResult.Success -> {
                    paymentEntity.status = PaymentStatus.COMPLETED
                    orderEntity.status = OrderStatus.DONE
                    logger.info { "$command 결제 성공, 주문 완료" }
                    return evaluationResult.toResult()
                }

                is PaymentApprovalResult.Failure -> {
                    paymentEntity.failReason = paymentResult.message
                    paymentEntity.status = PaymentStatus.FAILED
                    orderEntity.status = OrderStatus.PAYMENT_FAILURE
                    logger.info { "$command 결제 실패" }
                    return OrderPaymentApprovalResult.Failure(
                        message = paymentResult.message,
                        code = paymentResult.code,
                    )
                }
            }
        } catch (e: OptimisticLockingFailureException) {
            logger.info { "주문 승인 실패, 낙관적 락 동작" }
            throw OrderAlreadyInProgressException()
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

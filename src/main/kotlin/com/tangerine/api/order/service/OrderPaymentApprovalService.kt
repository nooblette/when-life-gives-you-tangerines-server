package com.tangerine.api.order.service

import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.mapper.toDomain
import com.tangerine.api.order.mapper.toDomains
import com.tangerine.api.order.mapper.toPaymentEvent
import com.tangerine.api.order.policy.OrderPaymentApprovalPolicy
import com.tangerine.api.order.repository.OrderCommandRepository
import com.tangerine.api.order.repository.OrderItemQueryRepository
import com.tangerine.api.order.repository.OrderQueryRepository
import com.tangerine.api.order.result.OrderPaymentApprovalResult
import com.tangerine.api.order.result.OrderPaymentEvaluationResult
import com.tangerine.api.order.service.command.ApproveOrderPaymentCommand
import mu.KotlinLogging
import org.springframework.context.ApplicationEventPublisher
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class OrderPaymentApprovalService(
    private val orderQueryRepository: OrderQueryRepository,
    private val orderItemQueryRepository: OrderItemQueryRepository,
    private val orderCommandRepository: OrderCommandRepository,
    private val approvalPolicy: OrderPaymentApprovalPolicy,
    private val eventPublisher: ApplicationEventPublisher,
) {
    @Transactional
    fun approve(approveOrderPaymentCommand: ApproveOrderPaymentCommand): OrderPaymentApprovalResult {
        try {
            logger.info { "주문 승인 프로세스 시작 - 요청 주문 정보 $approveOrderPaymentCommand" }
            val orderEntity =
                requireNotNull(orderQueryRepository.findByOrderId(approveOrderPaymentCommand.orderId)) {
                    "주문을 찾을 수 없습니다."
                }

            logger.info { "주문 데이터 버전 : ${orderEntity.version}" }
            if (orderEntity.status != OrderStatus.INIT) {
                throw IllegalStateException("이미 결제 완료된 주문")
            }

            val orderItems = orderItemQueryRepository.findByOrder(orderEntity).toDomains()
            val orderForPayment = orderEntity.toDomain(orderItems)

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
                    // 주문 상태 변경 및 즉시 DB 반영
                    orderEntity.status = OrderStatus.IN_PROGRESS
                    // 불필요한 이벤트 발행 과정 제거, flush로 비즈니스 로직에서 버전 충돌을 빠르게 감지한다.
                    orderCommandRepository.flush()
                    logger.info { "주문 상태 변경 및 flush" }

                    // 결제 이벤트 발행
                    eventPublisher.publishEvent(orderForPayment.markAsInProgress().toPaymentEvent())
                    logger.info { "결제 이벤트 발행" }
                    logger.info { "주문 승인 완료" }
                    evaluationResult.toResult()
                }
            }
        } catch (e: OptimisticLockingFailureException) {
            logger.info { "주문 승인 실패, 낙관적 락 동작" }
            throw IllegalStateException("이미 처리된 요청입니다.")
        } catch (e: IllegalStateException) {
            logger.info { e.message }
            throw e
        }
    }

    private fun OrderPaymentEvaluationResult.toResult(): OrderPaymentApprovalResult =
        when (this) {
            is OrderPaymentEvaluationResult.Failure -> OrderPaymentApprovalResult.Failure(message, code)
            is OrderPaymentEvaluationResult.Success -> OrderPaymentApprovalResult.Success()
        }
}

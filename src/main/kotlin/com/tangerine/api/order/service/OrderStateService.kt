package com.tangerine.api.order.service

import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.exception.OrderAlreadyInProgressException
import com.tangerine.api.order.repository.OrderRepository
import mu.KotlinLogging
import org.springframework.dao.OptimisticLockingFailureException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

private val logger = KotlinLogging.logger {}

@Service
class OrderStateService(
    private val orderRepository: OrderRepository,
) {
    @Transactional
    fun markAsInProgress(orderEntity: OrderEntity) {
        try {
            orderEntity.status = OrderStatus.IN_PROGRESS
            logger.info { "주문 상태 ${OrderStatus.IN_PROGRESS}로 변경" }

            // 빠른 버전 충돌 인지를 위함
            orderRepository.flush()
        } catch (e: OptimisticLockingFailureException) {
            logger.info { "주문 승인 실패(id : ${orderEntity.id}) 엔티티 버전 충돌" }
            throw OrderAlreadyInProgressException()
        }
    }

    @Transactional
    fun changeToDone(orderEntity: OrderEntity) {
        orderEntity.status = OrderStatus.DONE
        logger.info { "주문 상태 ${OrderStatus.DONE}로 변경" }
    }

    @Transactional
    fun changeToPaymentFailure(orderEntity: OrderEntity) {
        orderEntity.status = OrderStatus.PAYMENT_FAILURE
        logger.info { "주문 상태 ${OrderStatus.PAYMENT_FAILURE}로 변경" }
    }
}

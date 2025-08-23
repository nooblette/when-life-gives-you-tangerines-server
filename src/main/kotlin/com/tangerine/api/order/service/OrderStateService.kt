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
            logger.info { "Order(id = ${orderEntity.id}, orderId = ${orderEntity.orderId}) 주문 상태 변경 (status = ${OrderStatus.IN_PROGRESS})" }

            // 빠른 버전 충돌 인지를 위함
            orderRepository.flush()
        } catch (e: OptimisticLockingFailureException) {
            logger.info { "Order(id = ${orderEntity.id}, orderId = ${orderEntity.orderId}) 주문 승인 실패, 사유 : 엔티티 버전 충돌)" }
            throw OrderAlreadyInProgressException()
        }
    }

    @Transactional
    fun changeToDone(orderEntity: OrderEntity) {
        orderEntity.status = OrderStatus.DONE
        logger.info { "Order(id = ${orderEntity.id}, orderId = ${orderEntity.orderId}) 주문 상태 변경 (status = ${OrderStatus.DONE})" }
    }

    @Transactional
    fun changeToPaymentFailure(orderEntity: OrderEntity) {
        orderEntity.status = OrderStatus.PAYMENT_FAILURE
        logger.info { "Order(id = ${orderEntity.id}, orderId = ${orderEntity.orderId}) 주문 상태 변경 (status = ${OrderStatus.PAYMENT_FAILURE})" }
    }
}

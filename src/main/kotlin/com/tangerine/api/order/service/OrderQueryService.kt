package com.tangerine.api.order.service

import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.mapper.toDomain
import com.tangerine.api.order.mapper.toDomains
import com.tangerine.api.order.repository.OrderItemRepository
import com.tangerine.api.order.repository.OrderRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderQueryService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
) {
    data class OrderWithEntity(
        val domain: Order,
        val entity: OrderEntity,
    )

    @Transactional(readOnly = true)
    fun getOrderByOrderId(orderId: String): Order = getOrderByOrderIdWithEntity(orderId).domain

    @Transactional(readOnly = true)
    fun getOrderByOrderIdWithEntity(orderId: String): OrderWithEntity {
        val orderEntity =
            orderRepository.findByOrderId(orderId)
                ?: throw IllegalArgumentException("잘못된 주문 Id(Id = $orderId) 입니다.")

        val orderItems = orderItemRepository.findByOrder(orderEntity).toDomains()

        return OrderWithEntity(
            domain = orderEntity.toDomain(orderItems),
            entity = orderEntity,
        )
    }
}

package com.tangerine.api.order.service

import com.tangerine.api.order.domain.Order
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
    @Transactional(readOnly = true)
    fun getOrderById(orderId: String): Order {
        val order = requireNotNull(orderRepository.findByOrderId(orderId)) { "잘못된 주문 ID 입니다." }

        val orderItems =
            orderItemRepository
                .findByOrder(order)
                .toDomains()

        return order.toDomain(items = orderItems)
    }
}

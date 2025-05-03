package com.tangerine.api.order.service

import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.mapper.toDomain
import com.tangerine.api.order.mapper.toDomains
import com.tangerine.api.order.repository.OrderItemQueryRepository
import com.tangerine.api.order.repository.OrderQueryRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderQueryService(
    private val orderQueryRepository: OrderQueryRepository,
    private val orderItemQueryRepository: OrderItemQueryRepository,
) {
    @Transactional(readOnly = true)
    fun getOrderById(orderId: String): Order {
        val order = requireNotNull(orderQueryRepository.findByOrderId(orderId)) { "잘못된 주문 ID 입니다." }

        val orderItems =
            orderItemQueryRepository
                .findByOrder(order)
                .toDomains()

        return order.toDomain(items = orderItems)
    }
}

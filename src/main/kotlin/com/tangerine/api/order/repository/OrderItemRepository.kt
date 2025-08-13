package com.tangerine.api.order.repository

import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.entity.OrderItemEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderItemRepository : JpaRepository<OrderItemEntity, Long?> {
    fun findByOrder(order: OrderEntity): List<OrderItemEntity>
}

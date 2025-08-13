package com.tangerine.api.order.repository

import com.tangerine.api.order.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository

interface OrderRepository : JpaRepository<OrderEntity, Long?> {
    fun findByOrderId(orderId: String): OrderEntity?
}

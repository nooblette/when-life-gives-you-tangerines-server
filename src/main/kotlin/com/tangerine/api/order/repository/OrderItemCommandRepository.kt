package com.tangerine.api.order.repository

import com.tangerine.api.order.entity.OrderItemEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderItemCommandRepository : JpaRepository<OrderItemEntity, Long?>

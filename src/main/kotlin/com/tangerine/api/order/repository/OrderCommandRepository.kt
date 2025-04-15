package com.tangerine.api.order.repository

import com.tangerine.api.order.entity.OrderEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface OrderCommandRepository : JpaRepository<OrderEntity, Long?>

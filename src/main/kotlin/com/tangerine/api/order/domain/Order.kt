package com.tangerine.api.order.domain

import com.tangerine.api.order.common.OrderStatus

data class Order(
    val id: Long,
    val orderId: String,
    val customer: Customer,
    val items: List<OrderItem>,
    val totalAmount: Int,
    val status: OrderStatus,
)

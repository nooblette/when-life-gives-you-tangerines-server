package com.tangerine.api.order.fixture

import com.tangerine.api.order.domain.OrderItem

data class OrderItemInputs(
    val orderItemInputs: List<OrderItemInput>,
) {
    fun getTotalAmount(): Int = orderItemInputs.sumOf { it.item.price * it.quantity }

    fun toOrderItems(): List<OrderItem> = orderItemInputs.map { it.toOrderItem() }

    fun size() = orderItemInputs.size
}

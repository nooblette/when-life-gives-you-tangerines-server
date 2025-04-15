package com.tangerine.api.order.fixture

import com.tangerine.api.item.domain.Item
import com.tangerine.api.item.mapper.toOrderItem
import com.tangerine.api.order.domain.OrderItem

data class OrderItemInput(
    val item: Item,
    val quantity: Int,
) {
    fun toOrderItem(): OrderItem = item.toOrderItem(quantity)
}

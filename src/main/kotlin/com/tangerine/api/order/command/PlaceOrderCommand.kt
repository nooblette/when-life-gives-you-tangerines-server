package com.tangerine.api.order.command

import com.tangerine.api.order.domain.Customer
import com.tangerine.api.order.domain.OrderItem

data class PlaceOrderCommand(
    val customer: Customer,
    val items: List<OrderItem>,
    val totalAmount: Int,
    val orderName: String,
)

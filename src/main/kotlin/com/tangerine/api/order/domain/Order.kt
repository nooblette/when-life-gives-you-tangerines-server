package com.tangerine.api.order.domain

data class Order(
    val customer: Customer,
    val items: List<OrderItem>,
    val totalAmount: Int,
)

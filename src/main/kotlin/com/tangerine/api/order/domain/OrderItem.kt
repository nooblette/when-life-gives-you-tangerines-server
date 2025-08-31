package com.tangerine.api.order.domain

data class OrderItem(
    val id: Long,
    val name: String,
    val price: Int,
    val quantity: Int,
) {
    fun exceedsStock(availableStock: Int): Boolean = this.quantity > availableStock
}

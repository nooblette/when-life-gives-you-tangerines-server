package com.tangerine.api.order.domain

import com.tangerine.api.order.common.OrderStatus
import java.time.LocalDateTime

data class Order(
    val id: Long,
    val orderId: String,
    val customer: Customer,
    val items: List<OrderItem>,
    val totalAmount: Int,
    val status: OrderStatus,
    val createdAt: LocalDateTime,
) {
    fun isExpired(now: LocalDateTime): Boolean = status == OrderStatus.EXPIRED || createdAt.plusMinutes(EXPIRATION_MINUTES).isBefore(now)

    fun misMatches(totalAmount: Int): Boolean = this.totalAmount != totalAmount

    companion object {
        const val EXPIRATION_MINUTES: Long = 30L
    }
}

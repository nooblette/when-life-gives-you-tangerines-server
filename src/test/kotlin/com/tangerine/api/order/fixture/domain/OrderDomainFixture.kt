package com.tangerine.api.order.fixture.domain

import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.fixture.generator.TestOrderIdGenerator.Companion.STUB_ORDER_ID
import java.time.LocalDateTime
import java.util.concurrent.atomic.AtomicLong

object OrderDomainFixture {
    private val orderSeq = AtomicLong(1)

    fun createOrder(orderId: String = STUB_ORDER_ID): Order = order(orderId = orderId)

    fun createDoneOrder(orderId: String = STUB_ORDER_ID): Order =
        order(
            orderId = orderId,
            orderStatus = OrderStatus.DONE,
        )

    fun createInProgressOrder(orderId: String = STUB_ORDER_ID): Order =
        order(
            orderId = orderId,
            orderStatus = OrderStatus.IN_PROGRESS,
        )

    fun createExpiredOrderByStatus(orderId: String = STUB_ORDER_ID): Order =
        order(
            orderId = orderId,
            orderStatus = OrderStatus.EXPIRED,
        )

    fun createExpiredOrderByCreatedAt(
        orderId: String = STUB_ORDER_ID,
        createdAt: LocalDateTime,
    ): Order =
        order(
            orderId = orderId,
            createdAt = createdAt,
        )

    private fun order(
        orderId: String,
        orderItems: List<OrderItem> = createOrderItems(),
        orderStatus: OrderStatus = OrderStatus.INIT,
        createdAt: LocalDateTime = LocalDateTime.now(),
    ): Order =
        Order(
            id = orderSeq.getAndIncrement(),
            orderId = orderId,
            customer = createCustomer(),
            items = orderItems,
            totalAmount = orderItems.sumOf { it.price * it.quantity },
            status = orderStatus,
            createdAt = createdAt,
        )
}

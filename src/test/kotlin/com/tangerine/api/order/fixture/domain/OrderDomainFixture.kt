package com.tangerine.api.order.fixture.domain

import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.fixture.generator.TestOrderIdGenerator.Companion.STUB_ORDER_ID
import java.util.concurrent.atomic.AtomicLong

object OrderDomainFixture {
    private val orderSeq = AtomicLong(1)

    fun createOrder(orderId: String = STUB_ORDER_ID): Order {
        val orderItems = createOrderItems()

        return Order(
            id = orderSeq.getAndIncrement(),
            orderId = orderId,
            customer = createCustomer(),
            items = orderItems,
            totalAmount = orderItems.sumOf { it.price * it.quantity },
            status = OrderStatus.INIT,
        )
    }
}

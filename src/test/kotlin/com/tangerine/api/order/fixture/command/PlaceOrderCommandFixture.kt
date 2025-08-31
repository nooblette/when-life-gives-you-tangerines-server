package com.tangerine.api.order.fixture.command

import com.tangerine.api.order.command.PlaceOrderCommand
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.fixture.domain.createCustomer
import com.tangerine.api.order.fixture.domain.createOrderItems

fun createPlaceOrderCommand(testOrderItems: List<OrderItem> = createOrderItems()): PlaceOrderCommand =
    PlaceOrderCommand(
        customer = createCustomer(),
        items = testOrderItems,
        totalAmount = testOrderItems.sumOf { it.price * it.quantity },
        orderName = "테스트 주문",
    )

package com.tangerine.api.order.fixture.command

import com.tangerine.api.order.command.PlaceOrderCommand
import com.tangerine.api.order.fixture.domain.createCustomer
import com.tangerine.api.order.fixture.domain.createOrderItems

fun createPlaceOrderCommand(): PlaceOrderCommand {
    val testOrderItems = createOrderItems()

    return PlaceOrderCommand(
        customer = createCustomer(),
        items = testOrderItems,
        totalAmount = testOrderItems.sumOf { it.price * it.quantity },
    )
}

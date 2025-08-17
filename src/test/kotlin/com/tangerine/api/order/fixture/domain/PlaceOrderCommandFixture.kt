package com.tangerine.api.order.fixture.domain

import com.tangerine.api.order.command.PlaceOrderCommand

fun createPlaceOrderCommand(): PlaceOrderCommand {
    val testOrderItems = createOrderItems()

    return PlaceOrderCommand(
        customer = createCustomer(),
        items = testOrderItems,
        totalAmount = testOrderItems.sumOf { it.price * it.quantity },
    )
}

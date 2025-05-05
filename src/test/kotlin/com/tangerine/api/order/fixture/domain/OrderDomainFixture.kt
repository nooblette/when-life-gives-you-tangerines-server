package com.tangerine.api.order.fixture.domain

import com.tangerine.api.item.domain.Item
import com.tangerine.api.item.fixture.domain.ItemDomainFixture.createItems
import com.tangerine.api.order.domain.Customer
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.service.PlaceOrderCommand

fun createPlaceOrderCommand(): PlaceOrderCommand {
    val testOrderItems = createOrderItems()

    return PlaceOrderCommand(
        customer = createCustomer(),
        items = testOrderItems,
        totalAmount = testOrderItems.sumOf { it.price * it.quantity },
    )
}

fun createCustomer(): Customer =
    Customer(
        recipient = "이순신",
        name = "홍길동",
        phone = "010-1234-5678",
        address = "서울시 강남구 테헤란로 123",
        detailAddress = null,
        zipCode = "01234",
    )

fun createOrderItems(): List<OrderItem> = createItems().map(Item::toOrderItem)

fun Item.toOrderItem(quantity: Int = 1): OrderItem =
    OrderItem(
        id = this.id,
        name = this.name,
        price = this.price,
        quantity = quantity,
    )

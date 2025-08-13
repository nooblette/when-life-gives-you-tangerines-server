package com.tangerine.api.order.fixture.entity

import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.entity.OrderItemEntity

fun createOrderItemEntity(
    orderEntity: OrderEntity,
    itemId: Long = 100L,
    name: String = "테스트 상품",
    price: Int = 15000,
    quantity: Int = 2,
): OrderItemEntity =
    OrderItemEntity(
        itemId = itemId,
        name = name,
        price = price,
        quantity = quantity,
        order = orderEntity,
    )

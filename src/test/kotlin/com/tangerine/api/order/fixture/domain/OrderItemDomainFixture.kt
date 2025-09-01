package com.tangerine.api.order.fixture.domain

import com.tangerine.api.item.domain.Item
import com.tangerine.api.item.entity.ItemEntity
import com.tangerine.api.item.fixture.domain.ItemDomainFixture.createItems
import com.tangerine.api.order.domain.OrderItem

fun createOrderItems(): List<OrderItem> = createItems().map(Item::toOrderItem)

private fun Item.toOrderItem(quantity: Int = 1): OrderItem =
    OrderItem(
        id = this.id,
        name = this.name,
        price = this.price,
        quantity = quantity,
    )

fun createOrderItem(
    itemEntity: ItemEntity,
    quantity: Int,
): OrderItem =
    OrderItem(
        id = itemEntity.id ?: throw IllegalStateException("상품 Id가 없습니다."),
        name = itemEntity.name,
        price = itemEntity.price,
        quantity = quantity,
    )

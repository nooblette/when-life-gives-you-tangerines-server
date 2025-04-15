package com.tangerine.api.item.mapper

import com.tangerine.api.item.domain.Item
import com.tangerine.api.order.domain.OrderItem

fun Item.toOrderItem(quantity: Int): OrderItem =
    OrderItem(
        id = requireNotNull(this.id) { "OrderItem은 반드시 Item.id가 존재해야합니다." },
        name = name,
        price = price,
        quantity = quantity,
    )

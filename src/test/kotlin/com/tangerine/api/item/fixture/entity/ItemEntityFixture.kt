package com.tangerine.api.item.fixture.entity

import com.tangerine.api.item.common.UnitType
import com.tangerine.api.item.entity.ItemEntity

fun createItemEntity(
    name: String = "제주 노지 감귤 (10~15개입)",
    quantity: Int = 10,
    unit: UnitType = UnitType.KG,
    price: Int = 12000,
    stock: Int = 10,
): ItemEntity =
    ItemEntity(
        name = name,
        price = price,
        unit = unit,
        quantity = quantity,
        stock = stock,
    )

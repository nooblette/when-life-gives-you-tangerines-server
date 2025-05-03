package com.tangerine.api.item.fixture

import com.tangerine.api.item.common.UnitType
import com.tangerine.api.item.entity.ItemEntity

fun createTestItemEntity(create: (List<ItemEntity>) -> List<ItemEntity>): List<ItemEntity> {
    val item1 =
        ItemEntity(
            name = "제주 노지 감귤 (10~15개입)",
            price = 12000,
            unit = UnitType.KG,
            quantity = 10,
        )

    val item2 =
        ItemEntity(
            name = "제주 노지 감귤 (10~15개입)",
            price = 12000,
            unit = UnitType.KG,
            quantity = 10,
        )

    val itemEntities = listOf(item1, item2)
    create(itemEntities)
    return itemEntities
}

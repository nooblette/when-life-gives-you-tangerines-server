package com.tangerine.api.item.fixture.domain

import com.tangerine.api.item.common.UnitType
import com.tangerine.api.item.domain.Item
import java.util.concurrent.atomic.AtomicLong

object ItemDomainFixture {
    private val itemSeq = AtomicLong(1)

    fun createItems(): List<Item> =
        listOf(
            createItem(),
            createItem(
                name = "제주 노지 감귤 (20~25개입)",
                quantity = 15,
                price = 20000,
            ),
        )

    private fun createItem(
        name: String = "제주 노지 감귤 (10~15개입)",
        quantity: Int = 10,
        unit: UnitType = UnitType.KG,
        price: Int = 12000,
    ): Item =
        Item(
            id = itemSeq.getAndIncrement(),
            name = name,
            price = price,
            unit = unit,
            quantity = quantity,
            stock = 10,
        )
}

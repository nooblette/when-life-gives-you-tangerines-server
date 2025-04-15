package com.tangerine.api.item.mapper

import com.tangerine.api.item.domain.Item
import com.tangerine.api.item.entity.ItemEntity

fun List<ItemEntity>.toDomains(): List<Item> = map { it.toDomain() }

fun ItemEntity.toDomain(): Item =
    Item(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit,
        price = price,
    )

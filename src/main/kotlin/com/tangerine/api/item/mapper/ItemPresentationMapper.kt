package com.tangerine.api.item.mapper

import com.tangerine.api.item.api.response.ItemResponse
import com.tangerine.api.item.domain.Item

fun List<Item>.toResponses(): List<ItemResponse> = map { it.toResponse() }

private fun Item.toResponse(): ItemResponse =
    ItemResponse.of(
        id = id,
        name = name,
        quantity = quantity,
        unit = unit,
        price = price,
    )

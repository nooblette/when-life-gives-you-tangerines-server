package com.tangerine.api.item.api.response

import com.tangerine.api.item.common.UnitType

data class ItemResponse(
    val id: Long,
    val name: String,
    val weight: String,
    val price: Int,
    val stock: Int,
) {
    companion object {
        fun of(
            id: Long,
            name: String,
            quantity: Int,
            unit: UnitType,
            price: Int,
            stock: Int,
        ): ItemResponse = ItemResponse(id, name, quantity.toString() + unit, price, stock)
    }
}

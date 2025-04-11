package com.tangerine.api.item.common

enum class UnitType(
    private val label: String,
) {
    KG("kg"),
    ;

    override fun toString(): String = label
}

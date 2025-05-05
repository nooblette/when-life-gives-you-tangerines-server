package com.tangerine.api.item.domain

import com.tangerine.api.item.common.UnitType

data class Item(
    val id: Long,
    val name: String,
    val quantity: Int, // 수량 or 무게 수치
    val unit: UnitType, // 개입, kg 등
    val price: Int,
)

package com.tangerine.api.item.entity

import com.tangerine.api.item.common.UnitType
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id

@Entity(name = "item")
class ItemEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    val name: String,
    val quantity: Int, // 수량 or 무게 수치
    @Enumerated(EnumType.STRING)
    val unit: UnitType, // 개입, kg 등
    val price: Int,
)

package com.tangerine.api.item.entity

import com.tangerine.api.item.common.UnitType
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table

@Entity
@Table(name = "items")
class ItemEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val quantity: Int, // 수량 or 무게 수치
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val unit: UnitType, // 개입, kg 등
    @Column(nullable = false)
    val price: Int,
    @Column(nullable = false)
    var stock: Int,
)

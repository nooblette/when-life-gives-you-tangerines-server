package com.tangerine.api.order.entity

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "order_items")
class OrderItemEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val itemId: Long,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val price: Int,
    @Column(nullable = false)
    val quantity: Int,
    // 주문상품(OrderItems)와 주문(Order)는 N:1 매핑
    // 하나의 Order Id에 여러개의 상품(Item)이 속한다.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orderId", nullable = false)
    val order: OrderEntity? = null,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

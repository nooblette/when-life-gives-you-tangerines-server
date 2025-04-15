package com.tangerine.api.order.entity

import com.tangerine.api.order.common.OrderStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false, unique = true)
    val orderId: String,
    @Column(nullable = false)
    val name: String,
    @Column(nullable = false)
    val recipient: String,
    @Column(nullable = false)
    val phone: String,
    @Column(nullable = false)
    val address: String,
    val detailAddress: String?,
    @Column(nullable = false)
    val zipCode: String,
    @Column(nullable = false)
    val totalAmount: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val status: OrderStatus = OrderStatus.INIT,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    val updatedAt: LocalDateTime = LocalDateTime.now(),
)

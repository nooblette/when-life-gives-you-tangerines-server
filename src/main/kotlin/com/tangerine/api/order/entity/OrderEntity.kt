package com.tangerine.api.order.entity

import com.tangerine.api.order.common.OrderStatus
import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.PreUpdate
import jakarta.persistence.Table
import jakarta.persistence.Version
import java.time.LocalDateTime

@Entity
@Table(name = "orders")
class OrderEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Version
    var version: Long = 0,
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
    var status: OrderStatus = OrderStatus.INIT,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
) {
    @PreUpdate
    fun onUpdate() {
        this.updatedAt = LocalDateTime.now()
    }
}

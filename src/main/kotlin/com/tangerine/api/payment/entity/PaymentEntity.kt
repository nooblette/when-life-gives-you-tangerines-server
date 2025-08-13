package com.tangerine.api.payment.entity

import com.tangerine.api.payment.domain.PaymentStatus
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
@Table(name = "payments")
class PaymentEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
    @Column(nullable = false)
    val orderId: String,
    @Column(nullable = false)
    val amount: Int,
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: PaymentStatus,
    @Column(nullable = false)
    val paymentKey: String,
    var failReason: String? = null,
    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),
)

package com.tangerine.api.payment.repository

import com.tangerine.api.payment.entity.PaymentEntity
import org.springframework.data.jpa.repository.JpaRepository

interface PaymentRepository : JpaRepository<PaymentEntity, Long>

package com.tangerine.api.payment.fixture.entity

import com.tangerine.api.payment.domain.PaymentStatus
import com.tangerine.api.payment.entity.PaymentEntity
import org.springframework.jdbc.core.JdbcTemplate

fun countPaymentEntitiesByOrderId(
    jdbcTemplate: JdbcTemplate,
    orderId: String,
): Int =
    jdbcTemplate.queryForObject(
        "SELECT COUNT(*) FROM payments WHERE order_id = ?",
        Int::class.java,
        orderId,
    ) ?: 0

fun findPaymentEntityByOrderId(
    jdbcTemplate: JdbcTemplate,
    orderId: String,
): PaymentEntity? =
    jdbcTemplate.queryForObject(
        "SELECT id, order_id, payment_key, amount, status, fail_reason FROM payments WHERE order_id = ?",
        { rs, _ ->
            PaymentEntity(
                id = rs.getLong("id"),
                orderId = rs.getString("order_id"),
                paymentKey = rs.getString("payment_key"),
                amount = rs.getInt("amount"),
                status = PaymentStatus.valueOf(rs.getString("status")),
                failReason = rs.getString("fail_reason"),
            )
        },
        orderId,
    )

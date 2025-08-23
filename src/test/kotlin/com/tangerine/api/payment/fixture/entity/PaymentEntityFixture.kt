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
        """
        SELECT  id, 
                order_id, 
                order_name, 
                payment_key, 
                amount, 
                status, 
                requested_at, 
                approved_at, 
                fail_code, 
                fail_reason 
        FROM    payments 
        WHERE   order_id = ?
        """.trimIndent(),
        { rs, _ ->
            PaymentEntity(
                id = rs.getLong("id"),
                orderId = rs.getString("order_id"),
                orderName = rs.getString("order_name"),
                paymentKey = rs.getString("payment_key"),
                amount = rs.getInt("amount"),
                status = PaymentStatus.valueOf(rs.getString("status")),
                requestedAt = rs.getTimestamp("requested_at")?.toLocalDateTime(),
                approvedAt = rs.getTimestamp("approved_at")?.toLocalDateTime(),
                failCode = rs.getString("fail_code"),
                failReason = rs.getString("fail_reason"),
            )
        },
        orderId,
    )

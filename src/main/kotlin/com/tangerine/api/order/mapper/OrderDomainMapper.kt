package com.tangerine.api.order.mapper

import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.event.OrderPaymentEvent
import java.time.LocalDateTime

fun Order.toEntity(): OrderEntity =
    OrderEntity(
        id = this.id,
        orderId = this.orderId,
        name = this.customer.name,
        recipient = this.customer.recipient,
        phone = this.customer.phone,
        address = this.customer.address,
        detailAddress = this.customer.detailAddress,
        zipCode = this.customer.zipCode,
        totalAmount = this.totalAmount,
        status = this.status,
        createdAt = this.createdAt,
        updatedAt = LocalDateTime.now(),
    )

fun Order.toPaymentEvent(): OrderPaymentEvent =
    OrderPaymentEvent(
        orderId = this.orderId,
        totalAmount = this.totalAmount,
        customerName = this.customer.name,
        customerPhone = this.customer.phone,
    )

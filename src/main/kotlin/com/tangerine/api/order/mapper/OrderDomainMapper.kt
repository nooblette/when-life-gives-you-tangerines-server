package com.tangerine.api.order.mapper

import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.event.OrderPaymentEvent

fun Order.toPaymentEvent(): OrderPaymentEvent =
    OrderPaymentEvent(
        orderId = this.orderId,
        totalAmount = this.totalAmount,
        customerName = this.customer.name,
        customerPhone = this.customer.phone,
    )

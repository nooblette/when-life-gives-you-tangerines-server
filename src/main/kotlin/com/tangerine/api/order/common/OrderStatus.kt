package com.tangerine.api.order.common

enum class OrderStatus {
    INIT,
    IN_PROGRESS,
    PAYMENT_FAILURE,
    DONE,
    EXPIRED,
    SHIPMENT_REGISTERED,
}

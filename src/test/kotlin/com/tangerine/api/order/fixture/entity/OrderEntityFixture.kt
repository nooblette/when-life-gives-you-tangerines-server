package com.tangerine.api.order.fixture.entity

import com.tangerine.api.order.entity.OrderEntity

fun createOrderEntity(
    orderId: String,
    totalAmount: Int,
): OrderEntity =
    OrderEntity(
        orderId = orderId,
        orderName = "테스트 주문",
        customerName = "user 1",
        recipient = "user 1",
        phone = "01012345678",
        address = "서울특별시 중구",
        detailAddress = null,
        zipCode = "123456",
        totalAmount = totalAmount,
    )

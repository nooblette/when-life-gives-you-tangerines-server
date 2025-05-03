package com.tangerine.api.order.fixture

import com.tangerine.api.order.domain.Customer
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.service.PlaceOrderCommand

fun createPlaceOrderCommand(orderItemInputs: OrderItemInputs): PlaceOrderCommand =
    PlaceOrderCommand(
        customer = createTestCustomer(),
        items = createNewOrderItems(orderItemInputs),
        totalAmount = orderItemInputs.getTotalAmount(),
    )

fun createTestCustomer(): Customer =
    Customer(
        recipient = "이순신",
        name = "홍길동",
        phone = "010-1234-5678",
        address = "서울시 강남구 테헤란로 123",
        detailAddress = null,
        zipCode = "01234",
    )

fun createNewOrderItems(orderItemInputs: OrderItemInputs): List<OrderItem> = orderItemInputs.toOrderItems()

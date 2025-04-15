package com.tangerine.api.order.fixture

import com.tangerine.api.order.domain.Customer
import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.domain.OrderItem

fun createNewOrder(orderItemInputs: OrderItemInputs): Order =
    Order(
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

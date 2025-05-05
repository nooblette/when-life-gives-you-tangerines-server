package com.tangerine.api.order.fixture.domain

import com.tangerine.api.order.domain.Customer

fun createCustomer(): Customer =
    Customer(
        recipient = "이순신",
        name = "홍길동",
        phone = "010-1234-5678",
        address = "서울시 강남구 테헤란로 123",
        detailAddress = null,
        zipCode = "01234",
    )

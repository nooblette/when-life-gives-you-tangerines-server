package com.tangerine.api.order.common

enum class OrderStatus(
    val code: String,
) {
    INIT("0"),
    DONE("98"),
    EXPIRED("99"),
}

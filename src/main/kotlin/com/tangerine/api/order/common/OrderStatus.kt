package com.tangerine.api.order.common

enum class OrderStatus(
    val code: String,
) {
    INIT("0"),
    IN_PROGRESS("1"),
    DONE("98"),
    EXPIRED("99"),
}

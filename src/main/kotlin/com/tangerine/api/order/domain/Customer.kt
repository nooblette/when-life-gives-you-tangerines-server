package com.tangerine.api.order.domain

data class Customer(
    val name: String,
    val recipient: String,
    val phone: String,
    val address: String,
    val detailAddress: String?,
    val zipCode: String,
)

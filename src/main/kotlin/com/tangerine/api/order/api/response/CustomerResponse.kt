package com.tangerine.api.order.api.response

data class CustomerResponse(
    val name: String,
    val recipient: String,
    val phone: String,
    val address: String,
    val detailAddress: String?,
    val zipCode: String,
)

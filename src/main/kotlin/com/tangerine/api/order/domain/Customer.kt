package com.tangerine.api.order.domain

data class Customer(
    val name: String,
    val recipient: String,
    val phone: String,
    val address: String,
    val detailAddress: String?,
    val zipCode: String,
) {
    companion object {
        fun purchaser(
            name: String,
            recipient: String,
            phone: String,
            address: String,
            detailAddress: String?,
            zipCode: String,
        ) = Customer(
            name = name,
            recipient = recipient,
            phone = phone,
            address = address,
            detailAddress = detailAddress,
            zipCode = zipCode,
        )
    }
}

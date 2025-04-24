package com.tangerine.api.order.api.request

import jakarta.validation.constraints.NotBlank

data class CustomerRequest(
    @field:NotBlank
    val name: String?,
    @field:NotBlank
    val recipient: String?,
    @field:NotBlank
    val phone: String?,
    @field:NotBlank
    val address: String?,
    val detailAddress: String?,
    @field:NotBlank
    val zipCode: String?,
)

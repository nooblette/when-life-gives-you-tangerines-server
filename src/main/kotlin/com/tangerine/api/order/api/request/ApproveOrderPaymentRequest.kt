package com.tangerine.api.order.api.request

import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank

data class ApproveOrderPaymentRequest(
    @field:NotBlank
    val paymentKey: String,
    @field:Min(0, message = "주문금액은 음수(-)일 수 없습니다.")
    val totalAmount: Int,
)

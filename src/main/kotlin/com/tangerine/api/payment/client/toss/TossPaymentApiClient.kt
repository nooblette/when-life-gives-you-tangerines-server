package com.tangerine.api.payment.client.toss

import com.tangerine.api.payment.client.toss.request.ConfirmTossPaymentRequest
import com.tangerine.api.payment.client.toss.response.TossPayment
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader

@FeignClient(
    name = "toss-payment-api",
    url = "\${toss_payment.base_url}",
)
interface TossPaymentApiClient {
    @PostMapping("/v1/payments/confirm")
    fun confirmPayment(
        @RequestHeader("Authorization") authorization: String,
        @RequestBody request: ConfirmTossPaymentRequest,
    ): TossPayment
}

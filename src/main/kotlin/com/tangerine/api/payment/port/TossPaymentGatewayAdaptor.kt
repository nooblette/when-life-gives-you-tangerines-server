package com.tangerine.api.payment.port

import com.tangerine.api.payment.client.exception.ApiCallException
import com.tangerine.api.payment.client.toss.TossPaymentApiClient
import com.tangerine.api.payment.client.toss.exception.TossPaymentException
import com.tangerine.api.payment.mapper.toConfirmTossPaymentRequest
import com.tangerine.api.payment.request.ApprovePaymentRequest
import com.tangerine.api.payment.response.ApprovePaymentResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component

@Component
class TossPaymentGatewayAdaptor(
    @Value("\${toss_payment.secret_key}")
    private val secretKey: String,
    private val tossPaymentApiClient: TossPaymentApiClient,
) : PaymentGatewayPort {
    override fun approve(request: ApprovePaymentRequest): ApprovePaymentResponse =
        runCatching {
            tossPaymentApiClient.confirmPayment(
                authorization = secretKey,
                request = request.toConfirmTossPaymentRequest(),
            )
        }.fold(
            onSuccess = { ApprovePaymentResponse.Success(paymentKey = request.paymentKey, data = it) },
            onFailure = { exception -> failureHandler(paymentKey = request.paymentKey, exception = exception) },
        )

    private fun failureHandler(
        paymentKey: String,
        exception: Throwable,
    ) = when (exception) {
        is TossPaymentException ->
            ApprovePaymentResponse.Failure(
                paymentKey = paymentKey,
                code = exception.code,
                message = exception.message,
            )

        is ApiCallException ->
            ApprovePaymentResponse.Failure.apiCallError(
                paymentKey = paymentKey,
                message = exception.message,
            )

        else -> ApprovePaymentResponse.Failure.unknownError(paymentKey)
    }
}

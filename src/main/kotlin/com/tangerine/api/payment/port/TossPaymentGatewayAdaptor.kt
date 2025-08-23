package com.tangerine.api.payment.port

import com.tangerine.api.payment.client.exception.ApiCallException
import com.tangerine.api.payment.client.toss.TossPaymentApiClient
import com.tangerine.api.payment.client.toss.exception.TossPaymentException
import com.tangerine.api.payment.client.toss.response.TossPayment
import com.tangerine.api.payment.mapper.toConfirmTossPaymentRequest
import com.tangerine.api.payment.request.ApprovePaymentRequest
import com.tangerine.api.payment.response.ApprovePaymentResponse
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.time.LocalDateTime

private val logger = KotlinLogging.logger {}

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
            onSuccess = { successHandler(paymentKey = request.paymentKey, responseData = it) },
            onFailure = { failureHandler(paymentKey = request.paymentKey, exception = it) },
        )

    private fun successHandler(
        paymentKey: String,
        responseData: TossPayment,
    ): ApprovePaymentResponse {
        logger.info("Payment(PaymentKey = $paymentKey) 토스페이먼츠 결제 승인 성공 $responseData")
        return ApprovePaymentResponse.Success(
            paymentKey = paymentKey,
            orderName = responseData.orderName,
            requestAt = responseData.requestedAt,
            approvedAt = responseData.approvedAt ?: LocalDateTime.now(),
        )
    }

    private fun failureHandler(
        paymentKey: String,
        exception: Throwable,
    ): ApprovePaymentResponse =
        when (exception) {
            is TossPaymentException -> {
                logger.error(
                    "Payment(PaymentKey = $paymentKey) 토스페이먼츠 결제 승인 실패 " +
                        "(${exception.httpStatus.value()} ${exception.httpStatus.reasonPhrase}): " +
                        "${exception.code}(${exception.message})",
                )
                ApprovePaymentResponse.Failure(
                    paymentKey = paymentKey,
                    code = exception.code,
                    message = exception.message,
                )
            }

            is ApiCallException -> {
                logger.error(
                    "API call failed " +
                        "(${exception.httpStatus.value()} ${exception.httpStatus.reasonPhrase}): " +
                        "${ApiCallException.buildMessage(httpStatus = exception.httpStatus)})",
                )
                ApprovePaymentResponse.Failure.apiCallError(
                    paymentKey = paymentKey,
                    message = exception.message,
                )
            }

            else -> ApprovePaymentResponse.Failure.unknownError(paymentKey)
        }
}

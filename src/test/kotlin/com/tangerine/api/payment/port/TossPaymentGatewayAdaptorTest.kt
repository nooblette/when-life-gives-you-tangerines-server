package com.tangerine.api.payment.port

import com.tangerine.api.payment.client.toss.TossPaymentApiClient
import com.tangerine.api.payment.client.toss.exception.TossPaymentException
import com.tangerine.api.payment.client.toss.response.TossPayment
import com.tangerine.api.payment.fixture.client.toss.response.tossPayment
import com.tangerine.api.payment.request.ApprovePaymentRequest
import com.tangerine.api.payment.response.ApprovePaymentResponse
import feign.FeignException
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`
import org.mockito.kotlin.any

class TossPaymentGatewayAdaptorTest {
    private lateinit var tossPaymentApiClient: TossPaymentApiClient

    private lateinit var tossPaymentGatewayAdaptor: TossPaymentGatewayAdaptor

    private lateinit var request: ApprovePaymentRequest

    private val secretKey = "test_secret_key"

    @BeforeEach
    fun setUp() {
        tossPaymentApiClient = mock()
        tossPaymentGatewayAdaptor =
            TossPaymentGatewayAdaptor(
                secretKey = secretKey,
                tossPaymentApiClient = tossPaymentApiClient,
            )
        request =
            ApprovePaymentRequest(
                orderId = "test-order-123",
                amount = 15000,
                paymentKey = "test-payment-key-456",
            )
    }

    @Test
    fun `결제 승인 성공시 Success 응답을 반환한다`() {
        // given
        val tossPayment = tossPayment()
        `when`(
            tossPaymentApiClient.confirmPayment(any(), any()),
        ).thenReturn(tossPayment)

        // when
        val response = tossPaymentGatewayAdaptor.approve(request)

        // then
        response.shouldBeInstanceOf<ApprovePaymentResponse.Success<TossPayment>>()
        response.paymentKey shouldBe request.paymentKey
        response.data shouldBe tossPayment
    }

    @Test
    fun `TossPaymentException 발생시 Failure 응답을 반환한다`() {
        // given
        val exception = TossPaymentException.UnauthorizedKey()
        `when`(
            tossPaymentApiClient.confirmPayment(any(), any()),
        ).thenThrow(exception)

        // when
        val response = tossPaymentGatewayAdaptor.approve(request)

        // then
        response.shouldBeInstanceOf<ApprovePaymentResponse.Failure>()
        response.paymentKey shouldBe request.paymentKey
        response.code shouldBe exception.code
        response.message shouldBe exception.message
    }

    @Test
    fun `FeignException 발생시 apiCallError 응답을 반환한다`() {
        // given
        val feignException = mock<FeignException>()
        `when`(
            tossPaymentApiClient.confirmPayment(any(), any()),
        ).thenThrow(feignException)

        // when
        val response = tossPaymentGatewayAdaptor.approve(request)

        // then
        response.shouldBeInstanceOf<ApprovePaymentResponse.Failure>()
        response shouldBe ApprovePaymentResponse.Failure.apiCallError(request.paymentKey)
    }

    @Test
    fun `기타 예외 발생시 unknownError 응답을 반환한다`() {
        // given
        val runtimeException = RuntimeException("예기치 못한 오류")
        `when`(
            tossPaymentApiClient.confirmPayment(any(), any()),
        ).thenThrow(runtimeException)

        // when
        val response = tossPaymentGatewayAdaptor.approve(request)

        // then
        response.shouldBeInstanceOf<ApprovePaymentResponse.Failure>()
        response shouldBe ApprovePaymentResponse.Failure.unknownError(request.paymentKey)
    }
}

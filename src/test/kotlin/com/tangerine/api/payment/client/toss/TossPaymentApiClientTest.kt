package com.tangerine.api.payment.client.toss

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import com.github.tomakehurst.wiremock.client.WireMock.post
import com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo
import com.github.tomakehurst.wiremock.core.WireMockConfiguration
import com.tangerine.api.payment.client.toss.exception.TossPaymentException
import com.tangerine.api.payment.client.toss.request.ConfirmTossPaymentRequest
import com.tangerine.api.payment.client.toss.response.TossErrorResponse
import com.tangerine.api.payment.client.toss.response.TossPayment
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.HttpStatus
import org.springframework.test.context.TestPropertySource
import java.nio.charset.StandardCharsets
import java.util.Base64

@SpringBootTest
@TestPropertySource(
    properties = [
        "toss_payment.base_url=http://localhost:8089",
    ],
)
class TossPaymentApiClientTest {
    @Autowired
    private lateinit var tossPaymentApiClient: TossPaymentApiClient

    @Autowired
    private lateinit var objectMapper: ObjectMapper

    @Value("\${toss_payment.secret_key}")
    private lateinit var secretKey: String

    private lateinit var wireMockServer: WireMockServer

    private lateinit var request: ConfirmTossPaymentRequest

    @BeforeEach
    fun setUp() {
        wireMockServer = WireMockServer(WireMockConfiguration.options().port(8089))
        wireMockServer.start()
        request =
            ConfirmTossPaymentRequest(
                orderId = "test-order-123",
                amount = 15000,
                paymentKey = "test-payment-key-456",
            )
    }

    @AfterEach
    fun tearDown() {
        wireMockServer.stop()
    }

    @Test
    fun `결제 승인 API 호출 성공시 TossPayment 객체를 반환한다`() {
        // given
        val responseJson =
            """
            {
                "version": "2022-06-08",
                "paymentKey": "test-payment-key-456",
                "type": "NORMAL",
                "orderId": "test-order-123",
                "orderName": "생수 외 1건",
                "mId": "merchant_id",
                "currency": "KRW",
                "method": "카드",
                "totalAmount": 15000,
                "balanceAmount": 15000,
                "status": "DONE",
                "requestedAt": "2023-01-01T00:00:00",
                "approvedAt": "2023-01-01T00:00:00",
                "useEscrow": false,
                "lastTransactionKey": null,
                "suppliedAmount": 13636,
                "vat": 1364,
                "cultureExpense": false,
                "taxFreeAmount": 0,
                "taxExemptionAmount": 0,
                "isPartialCancelable": true,
                "country": "KR"
            }
            """.trimIndent()

        stubWireMockServer(
            httpStatus = HttpStatus.OK,
            responseJson = responseJson,
        )

        // when
        val result = tossPaymentApiClient.confirmPayment(createAuthorizationValue(), request)

        // then
        result shouldBe objectMapper.readValue(responseJson, TossPayment::class.java)
    }

    @ParameterizedTest
    @MethodSource("tossErrorResponseCases")
    fun `결제 승인 API 호출 실패시 TossPaymentException 예외로 래핑하여 던진다`(
        httpErrorStatus: HttpStatus,
        errorResponse: TossErrorResponse,
    ) {
        // given
        stubWireMockServer(
            httpStatus = httpErrorStatus,
            responseJson = objectMapper.writeValueAsString(errorResponse),
        )

        // when & then
        val exception =
            assertThrows<TossPaymentException> {
                tossPaymentApiClient.confirmPayment(createAuthorizationValue(), request)
            }

        exception.code shouldBe errorResponse.code
        exception.message shouldBe errorResponse.message
    }

    @Test
    fun `유효하지 않은 JSON 응답시 invalidJsonResponse 예외를 던진다`() {
        // given
        val httpStatus = HttpStatus.BAD_REQUEST
        val responseJson =
            """
            {
                "body": "invalid json"
            }
            """.trimIndent()
        stubWireMockServer(
            httpStatus = httpStatus,
            responseJson = responseJson,
        )

        // when
        val exception =
            assertThrows<TossPaymentException> {
                tossPaymentApiClient.confirmPayment(createAuthorizationValue(), request)
            }

        // then
        val expectedException = TossPaymentException.invalidJsonResponse(httpStatus = httpStatus, message = responseJson)
        exception.code shouldBe expectedException.code
        exception.message shouldBe expectedException.message
    }

    @Test
    fun `응답 body가 없으면 emptyBody 예외를 던진다`() {
        // given
        val httpStatus = HttpStatus.BAD_REQUEST
        wireMockServer.stubFor(
            post(urlEqualTo(TOSS_PAYMENT_CONFIRM_URL))
                .willReturn(
                    aResponse()
                        .withStatus(httpStatus.value())
                        .withHeader("Content-Type", "application/json"),
                ),
        )

        // when
        val exception =
            assertThrows<TossPaymentException> {
                tossPaymentApiClient.confirmPayment(createAuthorizationValue(), request)
            }

        // then
        val expectedException = TossPaymentException.emptyBody(httpStatus = httpStatus)
        exception.code shouldBe expectedException.code
        exception.message shouldBe expectedException.message
    }

    private fun createAuthorizationValue(): String =
        "Basic ${
        String(
            Base64.getEncoder().encode("$secretKey:".toByteArray(StandardCharsets.UTF_8)),
        )
        }"

    private fun stubWireMockServer(
        httpStatus: HttpStatus,
        responseJson: String,
    ) {
        wireMockServer.stubFor(
            post(urlEqualTo(TOSS_PAYMENT_CONFIRM_URL))
                .willReturn(
                    aResponse()
                        .withStatus(httpStatus.value())
                        .withHeader("Content-Type", "application/json")
                        .withBody(responseJson),
                ),
        )
    }

    companion object {
        const val TOSS_PAYMENT_CONFIRM_URL = "/v1/payments/confirm"

        @JvmStatic
        fun tossErrorResponseCases(): List<Arguments> =
            listOf(
                Arguments.of(
                    HttpStatus.BAD_REQUEST,
                    TossErrorResponse(
                        code = "INVALID_REQUEST",
                        message = "잘못된 요청입니다.",
                    ),
                ),
                Arguments.of(
                    HttpStatus.UNAUTHORIZED,
                    TossErrorResponse(
                        code = "UNAUTHORIZED_KEY",
                        message = "인증되지 않은 시크릿 키 혹은 클라이언트 키 입니다.",
                    ),
                ),
                Arguments.of(
                    HttpStatus.FORBIDDEN,
                    TossErrorResponse(
                        code = "REJECT_ACCOUNT_PAYMENT",
                        message = "잔액부족으로 결제에 실패했습니다.",
                    ),
                ),
                Arguments.of(
                    HttpStatus.NOT_FOUND,
                    TossErrorResponse(
                        code = "NOT_FOUND_PAYMENT",
                        message = "존재하지 않는 결제 정보 입니다.",
                    ),
                ),
                Arguments.of(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    TossErrorResponse(
                        code = "UNKNOWN_PAYMENT_ERROR",
                        message = "결제에 실패했어요. 같은 문제가 반복된다면 은행이나 카드사로 문의해주세요.",
                    ),
                ),
            )
    }
}

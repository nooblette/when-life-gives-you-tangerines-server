package com.tangerine.api.order.controller

import com.tangerine.api.global.response.ErrorCodes
import com.tangerine.api.global.session.manager.SessionManager
import com.tangerine.api.order.exception.OrderAlreadyInProgressException
import com.tangerine.api.order.fixture.builder.JsonOrderPaymentApprovalRequestBuilder
import com.tangerine.api.order.fixture.domain.generator.TestOrderIdGenerator
import com.tangerine.api.order.result.ApproveOrderPaymentResult
import com.tangerine.api.order.result.EvaluateOrderPaymentResult
import com.tangerine.api.order.usecase.ApproveOrderPaymentUseCase
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post
import org.springframework.test.web.servlet.result.StatusResultMatchersDsl
import java.time.LocalDateTime

@WebMvcTest(OrderPaymentApprovalController::class)
class OrderPaymentApprovalControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var approveOrderPaymentUseCase: ApproveOrderPaymentUseCase

    @MockitoBean
    lateinit var sessionManager: SessionManager

    @BeforeEach
    fun setUp() {
        doNothing().`when`(sessionManager).validateAndExtendSession(any())
    }

    @Test
    fun `주문 결제 요청 본문이 누락되면 400에러를 반환한다`() {
        // given
        val requestOrder =
            JsonOrderPaymentApprovalRequestBuilder()
                .build()

        // when & then
        performOrderRequest(requestOrder)
            .assertErrorResponse(expectedErrorCode = ErrorCodes.MISSING_FIELD)
    }

    @ParameterizedTest
    @MethodSource("createOrderPaymentRequestsWithoutRequiredParameter")
    fun `필수 파라미터가 누락되면 400에러를 반환한다`(requestOrderPaymentWithoutRequiredParam: String) {
        // when & then
        performOrderRequest(requestOrderPaymentWithoutRequiredParam)
            .assertErrorResponse(expectedErrorCode = ErrorCodes.MISSING_FIELD)
    }

    @Test
    fun `주문 금액이 음수인 경우 400에러를 반환한다`() {
        // given
        val requestOrder =
            JsonOrderPaymentApprovalRequestBuilder()
                .withDefaultOrderPaymentApprovalRequest()
                .withMinusTotalAmount()
                .build()

        // when & then
        performOrderRequest(requestOrder)
            .assertErrorResponse(expectedErrorCode = ErrorCodes.INVALID_ARGUMENT)
    }

    @Test
    fun `대상 주문에 대해 결제 검증을 실패한 경우 400에러를 반환한다`() {
        // given
        val requestOrder =
            JsonOrderPaymentApprovalRequestBuilder()
                .withDefaultOrderPaymentApprovalRequest()
                .build()
        val errorCode = "FAIL"
        whenever(approveOrderPaymentUseCase.approve(any()))
            .thenReturn(ApproveOrderPaymentResult.Failure("실패", errorCode))

        // when & then
        performOrderRequest(requestOrder)
            .assertErrorResponse(expectedErrorCode = errorCode)
    }

    @Test
    fun `동일한 주문 id에 대해 동시 주문 결제 승인 요청이 발생하는 경우 409에러를 반환한다`() {
        // given
        val requestOrder =
            JsonOrderPaymentApprovalRequestBuilder()
                .withDefaultOrderPaymentApprovalRequest()
                .build()
        whenever(approveOrderPaymentUseCase.approve(any()))
            .thenThrow(OrderAlreadyInProgressException())

        // when & then
        performOrderRequest(requestOrder)
            .assertErrorResponse(
                expectedStatus = { isConflict() },
                expectedErrorCode = EvaluateOrderPaymentResult.InProgressOrder().code,
            )
    }

    @Test
    fun `주문 결제 요청 성공 테스트`() {
        // given
        val requestOrder =
            JsonOrderPaymentApprovalRequestBuilder()
                .withDefaultOrderPaymentApprovalRequest()
                .build()
        whenever(approveOrderPaymentUseCase.approve(any()))
            .thenReturn(
                ApproveOrderPaymentResult.Success(
                    orderId = TestOrderIdGenerator.STUB_ORDER_ID,
                    orderName = "테스트 주문",
                    paymentKey = "TEST",
                    paymentMethod = "CARD",
                    totalAmount = 32000,
                    requestedAt = LocalDateTime.now(),
                    approvedAt = LocalDateTime.now(),
                ),
            )

        // when & then
        performOrderRequest(requestOrder)
            .andDo { print() }
            .andExpect { status { isOk() } }
    }

    // Api 호출
    private fun performOrderRequest(requestJson: String): ResultActionsDsl =
        mockMvc.post(ORDER_PAYMENT_APPROVAL_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
            cookie(Cookie("sessionId", "123"))
        }

    // 응답 및 에러 코드 검증
    private fun ResultActionsDsl.assertErrorResponse(
        expectedStatus: StatusResultMatchersDsl.() -> Unit = { isBadRequest() },
        expectedErrorCode: String,
    ) {
        this
            .andDo {
                // 요청/응답 전체를 콘솔에 출력
                print()
            }.andExpect {
                status(expectedStatus)
                jsonPath("$.code") { value(expectedErrorCode) }
            }
    }

    companion object {
        const val ORDER_PAYMENT_APPROVAL_URL = "/orders/" + TestOrderIdGenerator.STUB_ORDER_ID + "/payments"

        @JvmStatic
        fun createOrderPaymentRequestsWithoutRequiredParameter(): List<String> {
            val withoutPaymentKey =
                JsonOrderPaymentApprovalRequestBuilder()
                    .withDefaultOrderPaymentApprovalRequest()
                    .withoutPaymentKey()
                    .build()

            val withoutTotalAmount =
                JsonOrderPaymentApprovalRequestBuilder()
                    .withDefaultOrderPaymentApprovalRequest()
                    .withoutTotalAmount()
                    .build()

            return listOf(withoutPaymentKey, withoutTotalAmount)
        }
    }
}

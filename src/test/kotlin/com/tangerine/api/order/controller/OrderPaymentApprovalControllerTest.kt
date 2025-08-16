package com.tangerine.api.order.controller

import com.tangerine.api.global.response.ErrorCodes
import com.tangerine.api.order.fixture.builder.JsonOrderPaymentApprovalRequestBuilder
import com.tangerine.api.order.fixture.generator.TestOrderIdGenerator
import com.tangerine.api.order.result.OrderPaymentApprovalResult
import com.tangerine.api.order.usecase.ApproveOrderPaymentUseCase
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post

@WebMvcTest(OrderPaymentApprovalController::class)
class OrderPaymentApprovalControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var approveOrderPaymentUseCase: ApproveOrderPaymentUseCase

    @Test
    fun `주문 결제 요청 본문이 누락되면 400에러를 반환한다`() {
        // given
        val requestOrder =
            JsonOrderPaymentApprovalRequestBuilder()
                .build()

        // when & then
        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.MISSING_FIELD)
    }

    @ParameterizedTest
    @MethodSource("createOrderPaymentRequestsWithoutRequiredParameter")
    fun `필수 파라미터가 누락되면 400에러를 반환한다`(requestOrderPaymentWithoutRequiredParam: String) {
        // when & then
        performOrderRequest(requestOrderPaymentWithoutRequiredParam)
            .assertResponseCode(ErrorCodes.MISSING_FIELD)
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
            .assertResponseCode(ErrorCodes.INVALID_ARGUMENT)
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
            .thenReturn(OrderPaymentApprovalResult.Failure("실패", errorCode))

        // when & then
        performOrderRequest(requestOrder)
            .assertResponseCode(errorCode)
    }

    @Test
    fun `주문 결제 요청 성공 테스트`() {
        // given
        val requestOrder =
            JsonOrderPaymentApprovalRequestBuilder()
                .withDefaultOrderPaymentApprovalRequest()
                .build()
        whenever(approveOrderPaymentUseCase.approve(any()))
            .thenReturn(OrderPaymentApprovalResult.Success())

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
        }

    // 응답 및 에러 코드 검증
    private fun ResultActionsDsl.assertResponseCode(errorCode: String) {
        this
            .andDo {
                print() // 요청/응답 전체를 콘솔에 출력
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value(errorCode) }
            }
    }

    companion object {
        const val ORDER_PAYMENT_APPROVAL_URL = "/orders/" + TestOrderIdGenerator.STUB_ORDER_ID + "/payment-approval"

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

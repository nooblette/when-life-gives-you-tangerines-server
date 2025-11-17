package com.tangerine.api.order.controller

import com.tangerine.api.global.response.ErrorCodes
import com.tangerine.api.global.session.manager.SessionManager
import com.tangerine.api.item.exception.StockLockTimeoutException
import com.tangerine.api.order.fixture.builder.JsonOrderRequestBuilder
import com.tangerine.api.order.fixture.builder.OrderRequestBuilder
import com.tangerine.api.order.fixture.domain.generator.TestOrderIdGenerator
import com.tangerine.api.order.result.PlaceOrderResult
import com.tangerine.api.order.usecase.PlaceOrderUseCase
import jakarta.servlet.http.Cookie
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.junit.jupiter.params.provider.ValueSource
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

@WebMvcTest(OrderCommandController::class)
class OrderCommandControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var placeOrderUseCase: PlaceOrderUseCase

    @MockitoBean
    lateinit var sessionManager: SessionManager

    @BeforeEach
    fun setUp() {
        doNothing().`when`(sessionManager).validateAndExtendSession(any())
    }

    @Test
    fun `주문 요청 중 주문 정보 고객 필드가 누락되면 400에러를 반환한다`() {
        val requestOrder =
            JsonOrderRequestBuilder()
                .withoutCustomer()
                .withDefaultItems()
                .withDefaultTotalAmount()
                .build()

        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.MISSING_FIELD)
    }

    @Test
    fun `주문 요청 중 주문 상품 필드가 누락되면 400에러를 반환한다`() {
        val requestOrder =
            JsonOrderRequestBuilder()
                .withDefaultCustomer()
                .withoutItems()
                .withDefaultTotalAmount()
                .build()

        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.MISSING_FIELD)
    }

    @Test
    fun `주문 요청 중 총 금액 필드가 누락되면 400에러를 반환한다`() {
        val requestOrder =
            JsonOrderRequestBuilder()
                .withDefaultCustomer()
                .withDefaultItems()
                .withoutTotalAmount()
                .build()

        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.MISSING_FIELD)
    }

    @Test
    fun `주문 요청 중 주문 정보 고객의 필수 값이 누락되면 400에러를 반환한다`() {
        val requestOrder =
            JsonOrderRequestBuilder()
                .withDefaultCustomer()
                .withDefaultItems()
                .withDefaultTotalAmount()
                .withoutCustomerField(fieldName = "name")
                .build()

        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.MISSING_FIELD)
    }

    @Test
    fun `주문 요청 중 주문 상품의 필수 값이 누락되면 400에러를 반환한다`() {
        val requestOrder =
            JsonOrderRequestBuilder()
                .withDefaultCustomer()
                .withDefaultItems()
                .withDefaultTotalAmount()
                .withoutItemFieldAt(index = 0, fieldName = "price")
                .build()

        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.MISSING_FIELD)
    }

    @Test
    fun `주문 요청 중 주문 상품 리스트가 비어있으면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withEmptyItemList()
                .build()

        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.INVALID_ARGUMENT)
    }

    @Test
    fun `총 금액이 음수이면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withMinusTotalAmount()
                .build()

        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.INVALID_ARGUMENT)
    }

    @Test
    fun `주문 요청 중 주문 정보 고객 필드가 공백이면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withCustomerNameBlank()
                .build()

        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.INVALID_ARGUMENT)
    }

    @Test
    fun `주문 요청 중 주문 금액이 음수이면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withOrderItemMinusPrice()
                .build()

        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.INVALID_ARGUMENT)
    }

    @Test
    fun `주문 요청 중 주문 수량이 0이면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withOrderItemQuantityZero()
                .build()

        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.INVALID_ARGUMENT)
    }

    @ParameterizedTest
    @CsvSource(
        "name, 김철수박사님의아버지홍길동선생님과이순신장군님그리고세종대왕님까지모든분들께서함께하시는자리에서우리모두가하나가되어서평화롭게살아가며행복하게웃고있는모습들을보고",
        "recipient, 김철수박사님의아버지홍길동선생님과이순신장군님그리고세종대왕님까지모든분들께서함께하시는자리에서우리모두가하나가되어서평화롭게살아가며행복하게웃고있는모습들을보고",
        "address, 서울특별시 강남구 테헤란로 123번길 45-67 삼성타워 오피스텔 1234호 근처 편의점 앞 빨간 우체통 옆 카페에서 친구들과 만나기로 약속한장소",
        "detailAddress, 서울특별시 강남구 테헤란로 123번길 45-67 삼성타워 오피스텔 1234호 근처 편의점 앞 빨간 우체통 옆 카페에서 친구들과 만나기로 약속한장소",
    )
    fun `글자수 제한을 초과하면 400에러를 반환한다`(
        fieldName: String,
        value: String,
    ) {
        val requestOrder =
            JsonOrderRequestBuilder()
                .withDefaultCustomer()
                .withCustomerFieldAt(fieldName = fieldName, value = value)
                .withDefaultItems()
                .withDefaultTotalAmount()
                .withDefaultOrderName()
                .build()

        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.INVALID_ARGUMENT)
    }

    @ParameterizedTest
    @ValueSource(strings = ["010-123-4567", "02-123-4567", "01012345678", "010-1234-567"])
    fun `전화번호 형식이 xxx-xxxx-xxxx 어긋나면 400에러를 반환한다`(invalidPhoneNumber: String) {
        val requestOrder =
            JsonOrderRequestBuilder()
                .withDefaultCustomer()
                .withCustomerFieldAt(fieldName = "phone", value = invalidPhoneNumber)
                .withDefaultItems()
                .withDefaultTotalAmount()
                .withDefaultOrderName()
                .build()

        performOrderRequest(requestOrder)
            .assertResponseCode(ErrorCodes.INVALID_ARGUMENT)
    }

    @Test
    fun `동일한 상품에 대해 동시에 재고를 차감하는 경우 409에러를 반환한다`() {
        // when
        val requestOrder =
            JsonOrderRequestBuilder()
                .withDefaultCustomer()
                .withDefaultItems()
                .withDefaultTotalAmount()
                .withDefaultOrderName()
                .build()
        whenever(placeOrderUseCase.place(any()))
            .thenThrow(StockLockTimeoutException(message = "잠시 후 다시 시도해주세요."))

        // then
        performOrderRequest(requestOrder)
            .assertResponseCode(
                expectedStatus = { isConflict() },
                expectedErrorCode = ErrorCodes.CONFLICT,
            )
    }

    @Test
    fun `주문 요청 성공 테스트`() {
        // given
        val orderId = TestOrderIdGenerator.STUB_ORDER_ID
        whenever(placeOrderUseCase.place(any()))
            .thenReturn(PlaceOrderResult.Success(orderId))

        // when
        val requestOrder =
            JsonOrderRequestBuilder()
                .withDefaultCustomer()
                .withDefaultItems()
                .withDefaultTotalAmount()
                .withDefaultOrderName()
                .build()

        // then
        performOrderRequest(requestOrder)
            .andDo {
                print() // 요청/응답 전체를 콘솔에 출력
            }.andExpect {
                status { isOk() }
                jsonPath("$.orderId") { value(orderId) }
            }
    }

    // Api 호출
    private fun performOrderRequest(requestJson: String): ResultActionsDsl =
        mockMvc.post(ORDER_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
            cookie(Cookie("sessionId", "123"))
        }

    // 응답 및 에러 코드 검증
    private fun ResultActionsDsl.assertResponseCode(
        expectedErrorCode: String,
        expectedStatus: StatusResultMatchersDsl.() -> Unit = { isBadRequest() },
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
        const val ORDER_URL = "/orders"
    }
}

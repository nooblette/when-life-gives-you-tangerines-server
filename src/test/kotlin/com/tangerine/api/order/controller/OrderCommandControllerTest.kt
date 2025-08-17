package com.tangerine.api.order.controller

import com.tangerine.api.global.response.ErrorCodes
import com.tangerine.api.order.fixture.builder.JsonOrderRequestBuilder
import com.tangerine.api.order.fixture.builder.OrderRequestBuilder
import com.tangerine.api.order.fixture.generator.TestOrderIdGenerator
import com.tangerine.api.order.result.PlaceOrderResult
import com.tangerine.api.order.service.OrderCommandService
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post

@WebMvcTest(OrderCommandController::class)
class OrderCommandControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var orderCommandService: OrderCommandService

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

    @Test
    fun `주문 요청 성공 테스트`() {
        // given
        val orderId = TestOrderIdGenerator.STUB_ORDER_ID
        whenever(orderCommandService.place(any()))
            .thenReturn(PlaceOrderResult.Success(orderId))

        // when
        val requestOrder =
            JsonOrderRequestBuilder()
                .withDefaultCustomer()
                .withDefaultItems()
                .withDefaultTotalAmount()
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
        const val ORDER_URL = "/order"
    }
}

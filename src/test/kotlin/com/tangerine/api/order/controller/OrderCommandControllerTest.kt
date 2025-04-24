package com.tangerine.api.order.controller

import com.tangerine.api.global.handler.GlobalExceptionHandler
import com.tangerine.api.global.response.ErrorCodes
import com.tangerine.api.order.fixture.OrderRequestBuilder
import com.tangerine.api.order.service.OrderCommandService
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.post

@WebMvcTest(OrderCommandController::class)
@Import(GlobalExceptionHandler::class)
class OrderCommandControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @MockitoBean
    lateinit var orderCommandService: OrderCommandService

    @Test
    fun `주문 요청 중 주문 정보 고객 필드가 누락되면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withoutCustomer()
                .build()

        performOrderRequest(requestOrder)
            .assertInvalidArgument()
    }

    @Test
    fun `주문 요청 중 주문 상품 필드가 누락되면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withoutItems()
                .build()

        performOrderRequest(requestOrder)
            .assertInvalidArgument()
    }

    @Test
    fun `주문 요청 중 주문 상품 리스트가 비어있으면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withEmptyItemList()
                .build()

        performOrderRequest(requestOrder)
            .assertInvalidArgument()
    }

    @Test
    fun `주문 요청 중 총 금액 필드가 누락되면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withoutTotalAmount()
                .build()

        performOrderRequest(requestOrder)
            .assertInvalidArgument()
    }

    @Test
    fun `총 금액이 음수이면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withMinusTotalAmount()
                .build()

        performOrderRequest(requestOrder)
            .assertInvalidArgument()
    }

    @Test
    fun `주문 요청 중 주문 정보 고객의 필수 값이 누락되면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withoutCustomerField(fieldName = "name")
                .withoutCustomerField(fieldName = "zipCode")
                .build()

        performOrderRequest(requestOrder)
            .assertInvalidArgument()
    }

    @Test
    fun `주문 요청 중 주문 상품의 id가 누락되면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withoutItemField(index = 0, fieldName = "id")
                .build()

        performOrderRequest(requestOrder)
            .assertInvalidArgument()
    }

    @Test
    fun `주문 요청 중 주문 상품의 이름이 누락되면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withoutItemField(index = 0, fieldName = "name")
                .build()

        performOrderRequest(requestOrder)
            .assertInvalidArgument()
    }

    @Test
    fun `주문 요청 중 주문 상품의 가격이 누락되면 400에러를 반환한다`() {
        val requestOrder =
            OrderRequestBuilder()
                .withoutItemField(index = 0, fieldName = "price")
                .build()

        performOrderRequest(requestOrder)
            .assertInvalidArgument()
    }

    // Api 호출
    private fun performOrderRequest(requestJson: String): ResultActionsDsl =
        mockMvc.post(ORDER_URL) {
            contentType = MediaType.APPLICATION_JSON
            content = requestJson
        }

    // 응답 및 에러 코드 검증
    private fun ResultActionsDsl.assertInvalidArgument() {
        this.andExpect {
            status { isBadRequest() }
            jsonPath("$.code") { value(ErrorCodes.INVALID_ARGUMENT) }
        }
    }

    companion object {
        const val ORDER_URL = "/order"
    }
}

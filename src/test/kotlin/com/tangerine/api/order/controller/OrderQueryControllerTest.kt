package com.tangerine.api.order.controller

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.tangerine.api.global.response.ErrorCodes
import com.tangerine.api.order.domain.Customer
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.fixture.domain.OrderDomainFixture.createOrder
import com.tangerine.api.order.fixture.domain.generator.TestOrderIdGenerator.Companion.STUB_ORDER_ID
import com.tangerine.api.order.service.OrderQueryService
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.MvcResult
import org.springframework.test.web.servlet.ResultActionsDsl
import org.springframework.test.web.servlet.get

@WebMvcTest(OrderQueryController::class)
class OrderQueryControllerTest {
    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var orderQueryService: OrderQueryService

    @Test
    fun `orderId에 해당하는 주문 정보가 없는 경우 400에러를 반환한다`() {
        whenever(orderQueryService.getOrderByOrderId(ORDER_ID))
            .thenThrow(IllegalArgumentException::class.java)

        performOrderRequest()
            .andDo {
                print() // 요청/응답 전체를 콘솔에 출력
            }.andExpect {
                status { isBadRequest() }
                jsonPath("$.code") { value(ErrorCodes.INVALID_ARGUMENT) }
            }
    }

    @Test
    fun `주문 상세 정보 조회 성공 테스트`() {
        // given
        val expectedOrder = createOrder(orderId = ORDER_ID)
        whenever(orderQueryService.getOrderByOrderId(ORDER_ID))
            .thenReturn(expectedOrder)

        // when
        val response =
            performOrderRequest()
                .andDo {
                    print() // 요청/응답 전체를 콘솔에 출력
                }.andExpect {
                    status { isOk() }
                }.andReturn()

        // then
        response.json<String>(path = "orderId") shouldBe ORDER_ID
        response.json<Customer>(path = "customer") shouldBe expectedOrder.customer
        response.json<List<OrderItem>>(path = "items") shouldBe expectedOrder.items
        response.json<Int>(path = "totalAmount") shouldBe expectedOrder.totalAmount
    }

    // Api 호출
    private fun performOrderRequest(): ResultActionsDsl =
        mockMvc.get(ORDER_URL, ORDER_ID) {
            contentType = MediaType.APPLICATION_JSON
        }

    private inline fun <reified T> MvcResult.json(path: String): T {
        // 전체 응답 파싱
        val root = objectMapper.readTree(this.response.contentAsString)

        // 각 노드 탐색
        val node =
            path
                .split(".")
                .fold(root) { curr, segment -> curr.get(segment) }
                ?: throw NoSuchElementException("JSON path '$path' not found")

        // reified T 정보를 담은 TypeReference 로 변환
        return objectMapper.convertValue(node, object : TypeReference<T>() {})
    }

    companion object {
        const val ORDER_URL = "/orders/{orderId}"
        const val ORDER_ID = STUB_ORDER_ID
    }
}

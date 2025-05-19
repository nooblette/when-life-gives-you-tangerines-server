package com.tangerine.api.order.service

import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.event.OrderPaymentEvent
import com.tangerine.api.order.fixture.domain.OrderDomainFixture.createOrder
import com.tangerine.api.order.fixture.domain.createApproveOrderPaymentCommand
import com.tangerine.api.order.mapper.toPaymentEvent
import com.tangerine.api.order.policy.OrderPaymentApprovalPolicy
import com.tangerine.api.order.result.OrderPaymentApprovalResult
import com.tangerine.api.order.result.OrderPaymentEvaluationResult
import com.tangerine.api.order.service.command.ApproveOrderPaymentCommand
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.doNothing
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.event.EventListener
import org.springframework.test.context.bean.override.mockito.MockitoBean

@SpringBootTest
@Import(OrderPaymentApprovalServiceTest.TestConfig::class)
class OrderPaymentApprovalServiceTest {
    @Autowired
    private lateinit var orderPaymentApprovalService: OrderPaymentApprovalService

    @Autowired
    private lateinit var testOrderPaymentListener: TestConfig.TestOrderPaymentListener

    @MockitoBean
    private lateinit var orderQueryService: OrderQueryService

    @MockitoBean
    private lateinit var orderCommandService: OrderCommandService

    @MockitoBean
    private lateinit var orderPaymentApprovalPolicy: OrderPaymentApprovalPolicy

    private lateinit var approvalCommand: ApproveOrderPaymentCommand

    @BeforeEach
    fun setUp() {
        approvalCommand = createApproveOrderPaymentCommand()
        testOrderPaymentListener.clear()
    }

    @Test
    fun `주문 Id에 해당하는 주문 정보가 없으면 예외를 던진다`() {
        // given
        whenever(orderQueryService.getOrderById(approvalCommand.orderId))
            .thenThrow(IllegalArgumentException("잘못된 주문 ID 입니다."))

        // when & then
        shouldThrow<IllegalArgumentException> { orderPaymentApprovalService.approve(approvalCommand) }
    }

    @Test
    fun `주문 승인 정책에 위반된 경우 결제 실패를 반환한다`() {
        // given
        val order = createOrder(orderId = approvalCommand.orderId)
        whenever(orderQueryService.getOrderById(approvalCommand.orderId)).thenReturn(order)
        whenever(
            orderPaymentApprovalPolicy.evaluate(
                order = order,
                totalAmountForPayment = approvalCommand.totalAmount,
            ),
        ).thenReturn(OrderPaymentEvaluationResult.MisMatchedTotalAmount(message = "실패", code = "FAIL"))

        // when
        val result = orderPaymentApprovalService.approve(approvalCommand)

        // then
        result.shouldBeInstanceOf<OrderPaymentApprovalResult.Failure>()
    }

    @Test
    fun `동일한 주문에 대해 동시에 결제 요청을 하는 경우 한 번만 결제되고 다른 요청은 예외를 던진다`() {
    }

    @Test
    fun `결제 성공 케이스, 주문 상태가 진행중으로 변경되고 결제 이벤트를 발행한다`() {
        // given
        val order = createOrder(orderId = approvalCommand.orderId)
        whenever(orderQueryService.getOrderById(approvalCommand.orderId)).thenReturn(order)
        whenever(
            orderPaymentApprovalPolicy.evaluate(
                order = order,
                totalAmountForPayment = approvalCommand.totalAmount,
            ),
        ).thenReturn(OrderPaymentEvaluationResult.Success())

        // 실제로 주문 상태 변경 로직이 호출되지는 않는다. (doNothing)
        val orderCaptor = argumentCaptor<Order>() // 전달받은 인자를 기록하여 검증에 사용
        doNothing().`when`(orderCommandService).update(orderCaptor.capture())

        // when
        val result = orderPaymentApprovalService.approve(approvalCommand)

        // then
        result.shouldBeInstanceOf<OrderPaymentApprovalResult.Success>()

        // 주문 상태 변경 로직(orderCommandService.update())을 모킹하고 전달받은 주문의 상태를 검증
        val updatedOrder = orderCaptor.firstValue
        updatedOrder.status shouldBe OrderStatus.IN_PROGRESS

        // 이벤트 발행 검증
        testOrderPaymentListener.isPublishAtOnce(order.toPaymentEvent()) shouldBe true
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun testOrderPaymentListener(): TestOrderPaymentListener = TestOrderPaymentListener()

        class TestOrderPaymentListener {
            private val receivedEvents = mutableListOf<OrderPaymentEvent>()

            @EventListener
            fun handle(event: OrderPaymentEvent) {
                receivedEvents.add(event)
            }

            fun clear() = receivedEvents.clear()

            fun isPublishAtOnce(event: OrderPaymentEvent): Boolean = receivedEvents.size == 1 && receivedEvents.contains(event)
        }
    }
}

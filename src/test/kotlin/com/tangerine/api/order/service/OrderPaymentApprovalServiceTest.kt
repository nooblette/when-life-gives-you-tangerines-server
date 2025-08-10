package com.tangerine.api.order.service

import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.entity.OrderItemEntity
import com.tangerine.api.order.event.OrderPaymentEvent
import com.tangerine.api.order.fixture.domain.createApproveOrderPaymentCommand
import com.tangerine.api.order.fixture.entity.createOrderEntity
import com.tangerine.api.order.fixture.entity.createOrderItemEntity
import com.tangerine.api.order.mapper.toDomain
import com.tangerine.api.order.mapper.toDomains
import com.tangerine.api.order.mapper.toPaymentEvent
import com.tangerine.api.order.policy.OrderPaymentApprovalPolicy
import com.tangerine.api.order.repository.OrderItemQueryRepository
import com.tangerine.api.order.repository.OrderQueryRepository
import com.tangerine.api.order.result.OrderPaymentApprovalResult
import com.tangerine.api.order.result.OrderPaymentEvaluationResult
import com.tangerine.api.order.service.command.ApproveOrderPaymentCommand
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.event.TransactionalEventListener

@SpringBootTest
@Import(OrderPaymentApprovalServiceTest.TestConfig::class)
class OrderPaymentApprovalServiceTest {
    @Autowired
    private lateinit var orderPaymentApprovalService: OrderPaymentApprovalService

    @MockitoBean
    private lateinit var orderQueryRepository: OrderQueryRepository

    @MockitoBean
    private lateinit var orderItemQueryRepository: OrderItemQueryRepository

    @MockitoBean
    private lateinit var orderPaymentApprovalPolicy: OrderPaymentApprovalPolicy

    @Autowired
    private lateinit var testOrderPaymentListener: TestConfig.TestOrderPaymentListener

    private lateinit var approvalCommand: ApproveOrderPaymentCommand

    private lateinit var orderEntity: OrderEntity

    private lateinit var orderItemEntities: List<OrderItemEntity>

    @BeforeEach
    fun setUp() {
        approvalCommand = createApproveOrderPaymentCommand()
        orderEntity = createOrderEntity(approvalCommand.orderId, approvalCommand.totalAmount)
        orderItemEntities =
            listOf(
                createOrderItemEntity(
                    orderEntity = orderEntity,
                    price = approvalCommand.totalAmount,
                    quantity = 1,
                ),
            )
        testOrderPaymentListener.clear()
    }

    @Test
    fun `주문 Id에 해당하는 주문 정보가 없으면 예외를 던진다`() {
        // when & then
        shouldThrow<IllegalArgumentException> { orderPaymentApprovalService.approve(approvalCommand) }
    }

    @Test
    fun `주문 승인 정책에 위반된 경우 결제 실패를 반환한다`() {
        // given
        whenever(orderQueryRepository.findByOrderId(approvalCommand.orderId)).thenReturn(orderEntity)
        whenever(orderItemQueryRepository.findByOrder(orderEntity)).thenReturn(orderItemEntities)
        whenever(
            orderPaymentApprovalPolicy.evaluate(
                order = orderEntity.toDomain(orderItemEntities.toDomains()),
                totalAmountForPayment = approvalCommand.totalAmount,
            ),
        ).thenReturn(OrderPaymentEvaluationResult.MisMatchedTotalAmount(message = "실패", code = "FAIL"))

        // when
        val result = orderPaymentApprovalService.approve(approvalCommand)

        // then
        result.shouldBeInstanceOf<OrderPaymentApprovalResult.Failure>()
    }

    @Test
    fun `결제 성공 케이스, 주문 상태가 진행중으로 변경되고 결제 이벤트를 발행한다`() {
        // given
        whenever(orderQueryRepository.findByOrderId(approvalCommand.orderId)).thenReturn(orderEntity)
        whenever(orderItemQueryRepository.findByOrder(orderEntity)).thenReturn(orderItemEntities)
        val order = orderEntity.toDomain(orderItemEntities.toDomains())
        whenever(
            orderPaymentApprovalPolicy.evaluate(
                order = order,
                totalAmountForPayment = approvalCommand.totalAmount,
            ),
        ).thenReturn(OrderPaymentEvaluationResult.Success())

        // when
        val result = orderPaymentApprovalService.approve(approvalCommand)

        // then
        result.shouldBeInstanceOf<OrderPaymentApprovalResult.Success>()

        // 이벤트 발행 검증
        testOrderPaymentListener.isPublishAtOnce(order.toPaymentEvent()) shouldBe true
    }

    @TestConfiguration
    class TestConfig {
        @Bean
        fun testOrderPaymentListener(): TestOrderPaymentListener = TestOrderPaymentListener()

        class TestOrderPaymentListener {
            private val receivedEvents = mutableListOf<OrderPaymentEvent>()

            @TransactionalEventListener
            fun handle(event: OrderPaymentEvent) {
                receivedEvents.add(event)
            }

            fun clear() = receivedEvents.clear()

            fun isPublishAtOnce(event: OrderPaymentEvent): Boolean = receivedEvents.size == 1 && receivedEvents.contains(event)
        }
    }
}

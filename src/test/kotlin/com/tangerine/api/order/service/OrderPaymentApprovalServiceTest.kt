package com.tangerine.api.order.service

import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.entity.OrderItemEntity
import com.tangerine.api.order.fixture.domain.createApproveOrderPaymentCommand
import com.tangerine.api.order.fixture.entity.createOrderEntity
import com.tangerine.api.order.fixture.entity.createOrderItemEntity
import com.tangerine.api.order.policy.OrderPaymentApprovalPolicy
import com.tangerine.api.order.repository.OrderItemRepository
import com.tangerine.api.order.repository.OrderRepository
import com.tangerine.api.order.result.OrderPaymentApprovalResult
import com.tangerine.api.order.result.OrderPaymentEvaluationResult
import com.tangerine.api.order.service.command.ApproveOrderPaymentCommand
import com.tangerine.api.payment.command.PaymentApprovalResult
import com.tangerine.api.payment.command.PaymentApproveCommand
import com.tangerine.api.payment.domain.PaymentStatus
import com.tangerine.api.payment.fixture.command.equals
import com.tangerine.api.payment.fixture.entity.countPaymentEntitiesByOrderId
import com.tangerine.api.payment.fixture.entity.findPaymentEntityByOrderId
import com.tangerine.api.payment.port.PaymentGatewayPort
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.never
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.concurrent.atomic.AtomicInteger

@SpringBootTest
class OrderPaymentApprovalServiceTest {
    @Autowired
    private lateinit var orderPaymentApprovalService: OrderPaymentApprovalService

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var orderItemRepository: OrderItemRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @MockitoBean
    private lateinit var orderPaymentApprovalPolicy: OrderPaymentApprovalPolicy

    @MockitoBean
    private lateinit var paymentGatewayPort: PaymentGatewayPort

    private lateinit var approvalCommand: ApproveOrderPaymentCommand
    private lateinit var orderEntity: OrderEntity
    private lateinit var orderItemEntity: OrderItemEntity

    @BeforeEach
    fun setUp() {
        approvalCommand =
            createApproveOrderPaymentCommand(
                orderId = "order-${orderIdCounter.incrementAndGet()}",
            )
        orderEntity =
            orderRepository.save(
                createOrderEntity(
                    orderId = approvalCommand.orderId,
                    totalAmount = approvalCommand.totalAmount,
                ),
            )
        orderItemEntity =
            orderItemRepository.save(
                createOrderItemEntity(
                    orderEntity = orderEntity,
                    price = approvalCommand.totalAmount,
                    quantity = 1,
                ),
            )
    }

    @AfterEach
    fun tearDown() {
        orderItemRepository.delete(orderItemEntity)

        // deleteById() : OrderEntity @Version 충돌로 인한 OptimisticLockingFailureException 방지를 위함
        orderRepository.deleteById(
            requireNotNull(orderEntity.id) {
                "테스트 데이터 정리에 필요한 orderEntity Id가 없습니다."
            },
        )
        jdbcTemplate.update(
            "DELETE FROM payments WHERE order_id = ?",
            approvalCommand.orderId,
        )
    }

    @Test
    fun `주문 Id에 해당하는 주문 정보가 없으면 예외를 던진다`() {
        // when & then
        shouldThrow<IllegalArgumentException> {
            orderPaymentApprovalService.approve(
                command = createApproveOrderPaymentCommand(orderId = "invalidOrderId"),
            )
        }
    }

    @ParameterizedTest
    @MethodSource("failureTestCases")
    fun `주문 승인 정책에 위반된 경우 주문 결제 승인 실패를 반환한다`(evaluationResult: OrderPaymentEvaluationResult.Failure) {
        // given
        whenever(
            orderPaymentApprovalPolicy.evaluate(
                order = any(),
                totalAmountForPayment = any(),
            ),
        ).thenReturn(evaluationResult)

        // when
        val result = orderPaymentApprovalService.approve(approvalCommand)

        // then
        // 반환 값 검증
        result.shouldBeInstanceOf<OrderPaymentApprovalResult.Failure>()
        result.code shouldBe evaluationResult.code
        result.message shouldBe evaluationResult.message

        // 결제 프로세스 미진행 검증
        countPaymentEntitiesByOrderId(
            jdbcTemplate = jdbcTemplate,
            orderId = approvalCommand.orderId,
        ) shouldBe 0
        verify(paymentGatewayPort, never()).approve(any())

        // 주문 상태 검증
        val actualOrder = checkNotNull(orderRepository.findByOrderId(approvalCommand.orderId))
        actualOrder.status shouldBe OrderStatus.INIT
    }

    @Test
    fun `결제 실패시 주문 상태를 PAYMENT_FAILURE로 남기고 주문 결제 승인 실패를 반환한다`() {
        // given
        whenever(
            orderPaymentApprovalPolicy.evaluate(
                order = any(),
                totalAmountForPayment = any(),
            ),
        ).thenReturn(OrderPaymentEvaluationResult.Success())

        val paymentApprovalFailure =
            PaymentApprovalResult.Failure(
                paymentKey = approvalCommand.paymentKey,
                message = "정지된 카드 입니다.",
                code = "INVALID_STOPPED_CARD",
            )
        whenever(
            paymentGatewayPort.approve(any()),
        ).thenReturn(paymentApprovalFailure)

        // when
        val result = orderPaymentApprovalService.approve(approvalCommand)

        // then
        // 반환 값 검증
        result.shouldBeInstanceOf<OrderPaymentApprovalResult.Failure>()
        result.code shouldBe paymentApprovalFailure.code
        result.message shouldBe paymentApprovalFailure.message

        // 결제 프로세스 & 엔티티 검증
        val paymentGatewayCaptor = argumentCaptor<PaymentApproveCommand>()
        verify(paymentGatewayPort, times(1)).approve(paymentGatewayCaptor.capture())
        paymentGatewayCaptor.allValues shouldHaveSize 1
        paymentGatewayCaptor.allValues.forEach {
            it.equals(orderPaymentCommand = approvalCommand) shouldBe true
        }

        val actualPayment =
            checkNotNull(
                findPaymentEntityByOrderId(
                    jdbcTemplate = jdbcTemplate,
                    orderId = approvalCommand.orderId,
                ),
            )
        actualPayment.status shouldBe PaymentStatus.FAILED
        actualPayment.failReason shouldBe paymentApprovalFailure.message

        // 주문 상태 검증
        val actualOrder = checkNotNull(orderRepository.findByOrderId(approvalCommand.orderId))
        actualOrder.status shouldBe OrderStatus.PAYMENT_FAILURE
    }

    @Test
    fun `주문 결제 성공 케이스, 주문 상태는 DONE, 결제 상태는 COMPLETED가 된다`() {
        // given
        whenever(
            orderPaymentApprovalPolicy.evaluate(
                order = any(),
                totalAmountForPayment = any(),
            ),
        ).thenReturn(OrderPaymentEvaluationResult.Success())

        val paymentGatewayCaptor = argumentCaptor<PaymentApproveCommand>()
        whenever(
            paymentGatewayPort.approve(any()),
        ).thenReturn(PaymentApprovalResult.Success(paymentKey = approvalCommand.paymentKey))

        // when
        val result = orderPaymentApprovalService.approve(approvalCommand)

        // then
        result.shouldBeInstanceOf<OrderPaymentApprovalResult.Success>()

        // 결제 프로세스 & 엔티티 검증
        verify(paymentGatewayPort, times(1)).approve(paymentGatewayCaptor.capture())
        paymentGatewayCaptor.allValues shouldHaveSize 1
        paymentGatewayCaptor.allValues.forEach {
            it.equals(orderPaymentCommand = approvalCommand) shouldBe true
        }

        val actualPayment =
            checkNotNull(
                findPaymentEntityByOrderId(
                    jdbcTemplate = jdbcTemplate,
                    orderId = approvalCommand.orderId,
                ),
            )
        actualPayment.status shouldBe PaymentStatus.COMPLETED

        // 주문 상태 검증 (엔티티 버전은 동시성 테스트에서 검증한다)
        val actualOrder = checkNotNull(orderRepository.findByOrderId(approvalCommand.orderId))
        actualOrder.status shouldBe OrderStatus.DONE
    }

    companion object {
        @JvmStatic
        fun failureTestCases() =
            listOf(
                Arguments.of(OrderPaymentEvaluationResult.CompletedOrder()),
                Arguments.of(OrderPaymentEvaluationResult.ExpiredOrder()),
                Arguments.of(OrderPaymentEvaluationResult.MisMatchedTotalAmount()),
                Arguments.of(OrderPaymentEvaluationResult.MisMatchedTotalAmount()),
            )

        private val orderIdCounter = AtomicInteger()
    }
}

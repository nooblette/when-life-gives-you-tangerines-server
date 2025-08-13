package com.tangerine.api.order.service

import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.entity.OrderItemEntity
import com.tangerine.api.order.exception.OrderAlreadyInProgressException
import com.tangerine.api.order.fixture.domain.createApproveOrderPaymentCommand
import com.tangerine.api.order.fixture.entity.createOrderEntity
import com.tangerine.api.order.fixture.entity.createOrderItemEntity
import com.tangerine.api.order.repository.OrderItemRepository
import com.tangerine.api.order.repository.OrderRepository
import com.tangerine.api.order.result.OrderPaymentApprovalResult
import com.tangerine.api.order.result.OrderPaymentEvaluationResult
import com.tangerine.api.order.result.OrderPaymentEvaluationResult.InProgressOrder
import com.tangerine.api.order.service.command.ApproveOrderPaymentCommand
import com.tangerine.api.payment.command.PaymentApprovalResult
import com.tangerine.api.payment.command.PaymentApproveCommand
import com.tangerine.api.payment.domain.PaymentStatus
import com.tangerine.api.payment.fixture.command.equals
import com.tangerine.api.payment.fixture.entity.findPaymentEntityByOrderId
import com.tangerine.api.payment.port.PaymentGatewayPort
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import mu.KotlinLogging
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.argumentCaptor
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.jdbc.core.JdbcTemplate
import org.springframework.test.context.bean.override.mockito.MockitoBean
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@SpringBootTest
class OrderPaymentApprovalServiceConcurrencyTest {
    @Autowired
    private lateinit var orderPaymentApprovalService: OrderPaymentApprovalService

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var orderItemRepository: OrderItemRepository

    @Autowired
    private lateinit var jdbcTemplate: JdbcTemplate

    @MockitoBean
    private lateinit var paymentGatewayPort: PaymentGatewayPort

    private lateinit var approvalCommand: ApproveOrderPaymentCommand
    private lateinit var orderId: String
    private lateinit var orderEntity: OrderEntity
    private lateinit var orderItemEntity: OrderItemEntity

    @BeforeEach
    fun setUp() {
        orderId = "order-concurrency-test-1"
        approvalCommand = createApproveOrderPaymentCommand(orderId = orderId)
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
    fun `동일한 주문에 대해 동시 결제 요청을 하는 경우 한 번만 결제되고 다른 요청은 실패 결과를 반환한다`() {
        // given
        val preOrderVersion = orderRepository.findByOrderId(orderId)?.version ?: 0
        val paymentGatewayCaptor = argumentCaptor<PaymentApproveCommand>()
        whenever(
            paymentGatewayPort.approve(any()),
        ).thenReturn(PaymentApprovalResult.Success(paymentKey = approvalCommand.paymentKey))

        // when
        val results =
            submitConcurrencyTask(
                task = orderPaymentApprovalService::approve,
                request = approvalCommand,
            )

        // then
        // 하나의 요청만 성공해야 한다.
        val successResults = results.filterIsInstance<TaskResult.Success<OrderPaymentApprovalResult>>()
        val paymentSuccessResults = successResults.filter { it.result is OrderPaymentApprovalResult.Success }
        paymentSuccessResults shouldHaveSize 1

        // 동시성 충돌로 인한 예외 (스레드 풀 크기만큼)
        val concurrencyFailures =
            results
                .filterIsInstance<TaskResult.Failure>()
                .filter { it.exception is OrderAlreadyInProgressException }
        concurrencyFailures.forEach { failureResult ->
            failureResult.exception.shouldBeInstanceOf<OrderAlreadyInProgressException>()
            failureResult.exception.message shouldBe InProgressOrder().message
        }

        // 비즈니스 실패 (이미 완료된 주문)
        val businessFailures =
            successResults
                .filter { it.result is OrderPaymentApprovalResult.Failure }
                .map { it.result as OrderPaymentApprovalResult.Failure }
                .filter { it.code == OrderPaymentEvaluationResult.CompletedOrder().code }

        // 총합 검증
        (paymentSuccessResults.size + concurrencyFailures.size + businessFailures.size) shouldBe THREAD_COUNT

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

        // 주문 엔티티 검증
        val actualOrder = checkNotNull(orderRepository.findByOrderId(approvalCommand.orderId))
        actualOrder.version shouldBe preOrderVersion + 2
        actualOrder.status shouldBe OrderStatus.DONE
    }

    private fun <T, R> submitConcurrencyTask(
        task: (T) -> R,
        request: T,
    ): List<TaskResult> =
        (1..THREAD_COUNT)
            .map {
                CompletableFuture.supplyAsync {
                    val commonPool = ForkJoinPool.commonPool()

                    logger.debug(
                        """Thread Pool 정보 - 현재 스레드 : ${Thread.currentThread().name}
                        "병렬 처리 레벨: ${commonPool.parallelism}"
                        "활성 스레드 수: ${commonPool.activeThreadCount}"
                        "실행 중인 스레드 수: ${commonPool.runningThreadCount}"
                        "대기 중인 작업 수: ${commonPool.queuedSubmissionCount}"
                        "총 풀 크기: ${commonPool.poolSize}"
                    """,
                    )

                    runTask(index = it, task = task, request = request)
                }
            }.map { it.get(20, TimeUnit.SECONDS) }

    private fun <T, R> runTask(
        index: Int,
        task: (T) -> R,
        request: T,
    ): TaskResult =
        try {
            val result = task(request)
            TaskResult.Success(index, result)
        } catch (e: Exception) {
            TaskResult.Failure(index, e)
        }

    sealed class TaskResult {
        data class Success<R>(
            val threadIndex: Int,
            val result: R,
        ) : TaskResult()

        data class Failure(
            val threadIndex: Int,
            val exception: Exception,
        ) : TaskResult()
    }

    companion object {
        const val THREAD_COUNT = 10
    }
}

package com.tangerine.api.order.service

import com.tangerine.api.order.event.OrderPaymentEvent
import com.tangerine.api.order.fixture.domain.createApproveOrderPaymentCommand
import com.tangerine.api.order.fixture.domain.createPlaceOrderCommand
import com.tangerine.api.order.policy.OrderPaymentApprovalPolicy
import com.tangerine.api.order.repository.OrderQueryRepository
import com.tangerine.api.order.result.OrderPaymentApprovalResult
import com.tangerine.api.order.result.OrderPaymentEvaluationResult
import com.tangerine.api.order.result.OrderPlacementResult
import com.tangerine.api.order.service.command.ApproveOrderPaymentCommand
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import mu.KotlinLogging
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.ApplicationEventPublisher
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Primary
import org.springframework.stereotype.Component
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.transaction.event.TransactionalEventListener
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager
import java.util.Collections
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit

private val logger = KotlinLogging.logger {}

@SpringBootTest
@Import(OrderPaymentApprovalServiceConcurrencyTest.TestConfig::class)
class OrderPaymentApprovalServiceConcurrencyTest {
    @Autowired
    private lateinit var orderPaymentApprovalService: OrderPaymentApprovalService

    @Autowired
    private lateinit var testOrderPaymentListener: TestConfig.TestOrderPaymentListener

    @Autowired
    private lateinit var orderCommandService: OrderCommandService

    @MockitoBean
    private lateinit var orderPaymentApprovalPolicy: OrderPaymentApprovalPolicy

    @Autowired
    private lateinit var orderQueryRepository: OrderQueryRepository

    private lateinit var approvalCommand: ApproveOrderPaymentCommand

    private lateinit var orderId: String

    @BeforeEach
    fun setUp() {
        val result = orderCommandService.place(createPlaceOrderCommand())
        check(result is OrderPlacementResult.Success) {
            "테스트 주문 생성 실패: $result"
        }
        orderId = result.orderId
        approvalCommand = createApproveOrderPaymentCommand(orderId = orderId)

        testOrderPaymentListener.clear()
    }

    @Test
    fun `동일한 주문에 대해 동시에 여러 결제 요청을 하는 경우 한 번만 결제되고 다른 요청은 예외를 던진다`() {
        // given
        whenever(
            orderPaymentApprovalPolicy.evaluate(
                order = any(),
                totalAmountForPayment = any(),
            ),
        ).thenReturn(OrderPaymentEvaluationResult.Success())

        // when
        val preOrderVersion = orderQueryRepository.findByOrderId(orderId)?.version ?: 0
        val results =
            submitConcurrencyTask(
                task = orderPaymentApprovalService::approve,
                request = approvalCommand,
            )

        // then
        // 결제 이벤트는 한 번만 발행되어야한다.
        testOrderPaymentListener.eventCount() shouldBe 1

        // 하나의 요청만 성공해야한다.
        val successResults = results.filterIsInstance<TaskResult.Success<OrderPaymentApprovalResult>>()
        successResults shouldHaveSize 1

        // 나머지 요청은 실패한다.
        val failureResults = results.filterIsInstance<TaskResult.Failure>()
        failureResults shouldHaveSize THREAD_COUNT - successResults.size
        failureResults.forEach {
            it.exception.shouldBeInstanceOf<IllegalStateException>()
        }
        orderQueryRepository.findByOrderId(orderId)?.version shouldBe preOrderVersion + 1
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

    @TestConfiguration
    class TestConfig {
        @Bean
        fun testOrderPaymentListener(): TestOrderPaymentListener = TestOrderPaymentListener()

        @Primary
        @Component
        class DebuggingEventPublisher(
            @Qualifier("applicationEventPublisher")
            private val delegate: ApplicationEventPublisher,
        ) : ApplicationEventPublisher {
            private val pendingEvents = Collections.synchronizedList(mutableListOf<Any>())

            override fun publishEvent(event: Any) {
                val threadName = Thread.currentThread().name
                val txActive = TransactionSynchronizationManager.isSynchronizationActive()

                if (txActive) {
                    pendingEvents.add(event)
                    println("[$threadName] ${event::class.simpleName} 이벤트 대기 큐에 추가. 현재 대기 중인 이벤트 수: ${pendingEvents.size}")

                    // 트랜잭션 완료 시 콜백 등록
                    TransactionSynchronizationManager.registerSynchronization(
                        object : TransactionSynchronization {
                            override fun afterCommit() {
                                pendingEvents.remove(event)
                                println("[$threadName] 커밋 성공 - 이벤트 처리됨: ${event::class.simpleName} 남은 대기 이벤트 수: ${pendingEvents.size}")
                            }

                            override fun afterCompletion(status: Int) {
                                when (status) {
                                    TransactionSynchronization.STATUS_ROLLED_BACK -> {
                                        pendingEvents.remove(event)
                                        println("[$threadName] 롤백 - 이벤트 삭제: ${event::class.simpleName} 남은 대기 이벤트 수: ${pendingEvents.size}")
                                    }
                                }
                            }
                        },
                    )
                }

                delegate.publishEvent(event)
            }
        }

        class TestOrderPaymentListener {
            private val receivedEvents = mutableListOf<OrderPaymentEvent>()

            @TransactionalEventListener
            fun handle(event: OrderPaymentEvent) {
                println("이벤트 처리 : ${Thread.currentThread().name}")
                receivedEvents.add(event)
            }

            fun clear() = receivedEvents.clear()

            fun eventCount(): Int = receivedEvents.size
        }
    }

    companion object {
        const val THREAD_COUNT = 10
    }
}

package com.tangerine.api.order.policy

import com.tangerine.api.common.time.CurrentTimeProvider
import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.fixture.domain.OrderDomainFixture.createDoneOrder
import com.tangerine.api.order.fixture.domain.OrderDomainFixture.createExpiredOrderByCreatedAt
import com.tangerine.api.order.fixture.domain.OrderDomainFixture.createExpiredOrderByStatus
import com.tangerine.api.order.fixture.domain.OrderDomainFixture.createInProgressOrder
import com.tangerine.api.order.fixture.domain.OrderDomainFixture.createOrder
import com.tangerine.api.order.result.OrderPaymentEvaluationResult
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class OrderPaymentApprovalPolicyTest {
    @InjectMocks
    private lateinit var approvalPolicy: OrderPaymentApprovalPolicy

    @Mock
    private lateinit var currentTimeProvider: CurrentTimeProvider

    @ParameterizedTest
    @MethodSource("createNotInitialOrders")
    fun `주문 상태가 초기가 아닌 경우 실패를 반환한다`(notInitialOrder: Order) {
        // when
        val result = approvalPolicy.evaluate(notInitialOrder, notInitialOrder.totalAmount)

        // then
        result.shouldBeInstanceOf<OrderPaymentEvaluationResult.CompletedOrder>()
    }

    @Test
    fun `주문 금액이 불일치하는 경우 실패를 반환한다`() {
        // given
        val doneOrder = createOrder()
        val misMatchedTotalAmount = doneOrder.totalAmount - 1000
        whenever(currentTimeProvider.now())
            .thenReturn(LocalDateTime.now())

        // when
        val result = approvalPolicy.evaluate(doneOrder, misMatchedTotalAmount)

        // then
        result.shouldBeInstanceOf<OrderPaymentEvaluationResult.MisMatchedTotalAmount>()
    }

    @Test
    fun `주문 유효 기간이 지난 경우(만료된 주문) 실패를 반환한다`() {
        // given
        val orderCreatedAt = LocalDateTime.of(2020, 1, 1, 0, 0)
        val expiredOrder = createExpiredOrderByCreatedAt(createdAt = orderCreatedAt)

        val expiredTime = orderCreatedAt.plusMinutes(Order.EXPIRATION_MINUTES + 1)
        whenever(currentTimeProvider.now())
            .thenReturn(expiredTime)

        // when
        val result = approvalPolicy.evaluate(expiredOrder, expiredOrder.totalAmount)

        // then
        result.shouldBeInstanceOf<OrderPaymentEvaluationResult.ExpiredOrder>()
    }

    @Test
    fun `주문 결제 정책을 통과하면 Success를 반환한다`() {
        // given
        val order = createOrder()
        whenever(currentTimeProvider.now())
            .thenReturn(LocalDateTime.now())

        // when
        val result = approvalPolicy.evaluate(order, order.totalAmount)

        // then
        result.shouldBeInstanceOf<OrderPaymentEvaluationResult.Success>()
    }

    companion object {
        @JvmStatic
        fun createNotInitialOrders(): List<Order> = listOf(createDoneOrder(), createInProgressOrder(), createExpiredOrderByStatus())
    }
}

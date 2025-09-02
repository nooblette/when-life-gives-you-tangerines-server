package com.tangerine.api.item.service

import com.tangerine.api.fixture.concurrency.TaskResult
import com.tangerine.api.fixture.concurrency.submitConcurrencyTask
import com.tangerine.api.item.entity.ItemEntity
import com.tangerine.api.item.fixture.entity.createItemEntity
import com.tangerine.api.item.repository.ItemRepository
import com.tangerine.api.item.result.DecreaseStockResult
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.fixture.domain.createOrderItem
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest

@SpringBootTest
class ItemStockServiceTest {
    @Autowired
    private lateinit var itemStockService: ItemStockService

    @Autowired
    private lateinit var itemRepository: ItemRepository

    private lateinit var itemEntity: ItemEntity

    @BeforeEach
    fun setUp() {
        itemEntity = itemRepository.save(createItemEntity(stock = STOCK))
    }

    @Test
    fun `대상 상품이 존재하지 않는 주문이 포함된 경우 IllegalArgumentException 예외를 던진다`() {
        // given
        val existOrderItem =
            createOrderItem(
                itemEntity = itemEntity,
                quantity = STOCK - 1,
            )

        val nonExistOrderItems =
            OrderItem(
                id = -1L,
                name = "잘못된 상품",
                price = 1000,
                quantity = 1,
            )

        // when & then
        shouldThrow<IllegalArgumentException> {
            itemStockService.decreaseStockByOrderItems(
                orderItems = listOf(existOrderItem, nonExistOrderItems),
            )
        }
    }

    @Test
    fun `주문 가능한 재고 수량을 초과한 경우 Failure 객체를 반환한다`() {
        // given
        val orderItem =
            createOrderItem(
                itemEntity = itemEntity,
                quantity = STOCK + 1,
            )

        // when
        val result = itemStockService.decreaseStockByOrderItems(orderItems = listOf(orderItem))

        // then
        result.shouldBeInstanceOf<DecreaseStockResult.Failure>()
    }

    @Test
    fun `상품 검증 성공시 주문 수량만큼 재고를 차감한다`() {
        // given
        val orderQuantity = STOCK / 2
        val orderItem =
            createOrderItem(
                itemEntity = itemEntity,
                quantity = orderQuantity,
            )

        // when
        val result = itemStockService.decreaseStockByOrderItems(orderItems = listOf(orderItem))

        // then
        result.shouldBeInstanceOf<DecreaseStockResult.Success>()

        // 재고 수량 차감 검증
        val updatedItem = itemRepository.findById(orderItem.id).get()
        updatedItem.stock shouldBe STOCK - orderQuantity
    }

    @Test
    fun `동일 상품에 동시 재고 차감 요청시 주문 수량만큼 재고가 정상 차감된다`() {
        // given
        val threadCount = STOCK + 5
        val orderQuantity = 1
        val orderItem =
            createOrderItem(
                itemEntity = itemEntity,
                quantity = orderQuantity,
            )

        // when
        val results =
            submitConcurrencyTask(
                task = itemStockService::decreaseStockByOrderItems,
                request = listOf(orderItem),
                threadCount = threadCount,
            )

        // then
        results.forEach { result -> result.shouldBeInstanceOf<TaskResult.Success<*>>() }

        val decreaseResults =
            results
                .filterIsInstance<TaskResult.Success<*>>()
                .map { it.result as DecreaseStockResult }
        val successCount = decreaseResults.count { it is DecreaseStockResult.Success }
        val failureCount = decreaseResults.count { it is DecreaseStockResult.Failure }

        // 비관적 락으로 순차 처리(동시성 제어) 10개는 성공, 나머지는 재고 부족으로 실패
        successCount shouldBe STOCK
        failureCount shouldBe threadCount - STOCK

        val updatedItem = itemRepository.findById(orderItem.id).get()
        updatedItem.stock shouldBe STOCK - successCount * orderQuantity
    }

    companion object {
        private const val STOCK = 10
    }
}

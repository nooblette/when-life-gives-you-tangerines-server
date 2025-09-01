package com.tangerine.api.order.usecase

import com.tangerine.api.item.exception.StockConflictException
import com.tangerine.api.item.fixture.entity.createItemEntity
import com.tangerine.api.item.repository.ItemRepository
import com.tangerine.api.item.result.DecreaseStockResult
import com.tangerine.api.item.service.ItemStockService
import com.tangerine.api.order.command.PlaceOrderCommand
import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.fixture.command.createPlaceOrderCommand
import com.tangerine.api.order.fixture.domain.createOrderItem
import com.tangerine.api.order.fixture.domain.generator.TestOrderIdGenerator
import com.tangerine.api.order.fixture.domain.generator.TestOrderIdGenerator.Companion.STUB_ORDER_ID
import com.tangerine.api.order.repository.OrderItemRepository
import com.tangerine.api.order.repository.OrderRepository
import com.tangerine.api.order.result.PlaceOrderResult
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.ArgumentMatchers.anyList
import org.mockito.kotlin.whenever
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.test.context.bean.override.mockito.MockitoSpyBean

@SpringBootTest
@Import(TestOrderIdGenerator::class)
class OrderPlaceUseCaseTest {
    @Autowired
    private lateinit var orderPlaceUseCase: OrderPlaceUseCase

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var orderItemRepository: OrderItemRepository

    @Autowired
    private lateinit var itemRepository: ItemRepository

    @MockitoSpyBean
    private lateinit var itemStockService: ItemStockService

    private lateinit var orderItems: List<OrderItem>

    private lateinit var newOrder: PlaceOrderCommand

    @BeforeEach
    fun setUp() {
        orderItems =
            listOf(
                createOrderItem(
                    itemEntity = itemRepository.save(createItemEntity(stock = STOCK)),
                    quantity = 1,
                ),
            )
        newOrder = createPlaceOrderCommand(testOrderItems = orderItems)
    }

    @Test
    fun `상품 재고 처리 중 예외가 발생하면 그대로 전파한다`() {
        // given
        whenever(
            itemStockService.decreaseStockByOrderItems(anyList()),
        ).thenThrow(StockConflictException("잠시 후 다시 시도해주세요."))

        // when & then
        shouldThrow<StockConflictException> {
            orderPlaceUseCase.place(command = newOrder)
        }
    }

    @Test
    fun `상품 재고 처리에 실패하면 Failure 객체를 반환한다`() {
        // given
        val message = "재고 처리 실패"
        whenever(
            itemStockService.decreaseStockByOrderItems(anyList()),
        ).thenReturn(DecreaseStockResult.Failure(message = message))

        // when
        val result = orderPlaceUseCase.place(command = newOrder)

        // then
        result shouldBe PlaceOrderResult.Failure("재고 처리 실패")
    }

    @Test
    fun `주문 생성 성공 테스트`() {
        // when
        val result = orderPlaceUseCase.place(command = newOrder)

        // then
        result.shouldBeInstanceOf<PlaceOrderResult.Success>()
        result.orderId shouldBe STUB_ORDER_ID

        // 주문 검증
        assertPlacementOrder(result)

        // 잔여 재고 수량 검증
        orderItems.forEach { orderItem -> assertDecreasedStock(orderItem) }
    }

    private fun assertPlacementOrder(result: PlaceOrderResult.Success) {
        val actualOrder = orderRepository.findByOrderId(orderId = result.orderId)

        actualOrder.shouldNotBeNull()
        actualOrder.status shouldBe OrderStatus.INIT
        actualOrder.orderId shouldBe STUB_ORDER_ID
        orderItemRepository.findByOrder(actualOrder) shouldHaveSize newOrder.items.size
    }

    private fun assertDecreasedStock(orderItem: OrderItem) {
        val updatedItem =
            itemRepository
                .findById(requireNotNull(orderItem.id))
                .orElse(null)

        updatedItem.shouldNotBeNull()
        updatedItem.stock shouldBe STOCK - orderItem.quantity
    }

    companion object {
        private const val STOCK = 10
    }
}

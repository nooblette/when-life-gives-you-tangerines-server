package com.tangerine.api.order.usecase

import com.tangerine.api.item.entity.ItemEntity
import com.tangerine.api.item.fixture.entity.createItemEntity
import com.tangerine.api.item.repository.ItemRepository
import com.tangerine.api.order.command.PlaceOrderCommand
import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.entity.OrderItemEntity
import com.tangerine.api.order.fixture.command.createPlaceOrderCommand
import com.tangerine.api.order.fixture.domain.generator.TestOrderIdGenerator
import com.tangerine.api.order.fixture.domain.generator.TestOrderIdGenerator.Companion.STUB_ORDER_ID
import com.tangerine.api.order.repository.OrderItemRepository
import com.tangerine.api.order.repository.OrderRepository
import com.tangerine.api.order.result.PlaceOrderResult
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.CsvSource
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(TestOrderIdGenerator::class)
@Transactional
class OrderPlaceUseCaseTest {
    @Autowired
    lateinit var orderPlaceUseCase: OrderPlaceUseCase

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var orderItemRepository: OrderItemRepository

    @Autowired
    lateinit var itemRepository: ItemRepository

    lateinit var itemEntity: ItemEntity

    lateinit var newOrder: PlaceOrderCommand

    private fun createAndSaveItem(stock: Int): ItemEntity = itemRepository.save(createItemEntity(stock = stock))

    private fun createOrderItem(
        item: ItemEntity,
        quantity: Int,
    ): OrderItem =
        OrderItem(
            id = item.id ?: throw IllegalArgumentException("상품 Id가 없습니다."),
            name = item.name,
            price = item.price,
            quantity = quantity,
        )

    @Test
    fun `주문 대상 상품이 존재하지 않는 경우 IllegalArgumentException 예외를 던진다`() {
        // when & then
        shouldThrow<IllegalArgumentException> {
            orderPlaceUseCase.place(command = createPlaceOrderCommand())
        }
    }

    @ParameterizedTest
    @CsvSource(
        value = [
            "10:'남은 수량보다 많이 주문할 수 없습니다.'",
            "0:'품절 상품은 주문할 수 없습니다.'",
        ],
        delimiter = ':',
    )
    fun `주문 가능한 재고 수량을 초과한 경우 Failure 객체를 반환한다`(
        itemStock: Int,
        message: String,
    ) {
        // given
        itemEntity = createAndSaveItem(itemStock)
        newOrder =
            createPlaceOrderCommand(
                testOrderItems =
                    listOf(
                        createOrderItem(itemEntity, itemStock + 1),
                    ),
            )

        // when
        val actualResult = orderPlaceUseCase.place(newOrder)

        // then
        actualResult shouldBe PlaceOrderResult.Failure(message)
    }

    @Test
    fun `주문 생성 성공 테스트`() {
        // given
        val itemStock = 10
        itemEntity = createAndSaveItem(itemStock)

        val orderItem = createOrderItem(itemEntity, 1)
        newOrder = createPlaceOrderCommand(testOrderItems = listOf(orderItem))

        // when
        val result = orderPlaceUseCase.place(command = newOrder)

        // then
        result.shouldBeInstanceOf<PlaceOrderResult.Success>()
        result.orderId shouldBe STUB_ORDER_ID

        // 주문 검증
        val actualOrder = orderRepository.findByOrderId(result.orderId)
        actualOrder shouldNotBe null
        actualOrder?.let {
            assertPlacementOrder(actualOrder)
            assertPlacementOrderItem(orderItemRepository.findByOrder(actualOrder))
        }

        // 잔여 재고 수량 검증
        val actualRemainingStock =
            itemRepository
                .findById(requireNotNull(itemEntity.id))
                .orElseThrow { IllegalArgumentException("${itemEntity.id}에 해당하는 상품이 없습니다") }
                .stock
        actualRemainingStock shouldBe itemStock - orderItem.quantity
    }

    private fun assertPlacementOrder(actualOrder: OrderEntity) {
        // 주문 생성시 초기 상태는 INIT
        actualOrder.status shouldBe OrderStatus.INIT
        actualOrder.orderName shouldBe newOrder.orderName
        actualOrder.customerName shouldBe newOrder.customer.name
        actualOrder.recipient shouldBe newOrder.customer.recipient
        actualOrder.phone shouldBe newOrder.customer.phone
        actualOrder.address shouldBe newOrder.customer.address
        actualOrder.detailAddress shouldBe newOrder.customer.detailAddress
        actualOrder.zipCode shouldBe newOrder.customer.zipCode
        actualOrder.totalAmount shouldBe newOrder.totalAmount
    }

    private fun assertPlacementOrderItem(actualOrderItem: List<OrderItemEntity>) {
        actualOrderItem shouldNotBe null
        actualOrderItem shouldHaveSize newOrder.items.size

        // 테스트 실행 순서에 따라 orderItemEntity의 Id 값이 달라져 테스트 격리 원칙에 위배
        // Id 생성은 Jpa의 책임이므로 비즈니스 로직에서 검증하지 않는다.
        actualOrderItem.forEachIndexed { index, orderItemEntity ->
            val expectedItem = newOrder.items[index]
            orderItemEntity.itemId shouldBe expectedItem.id
            orderItemEntity.name shouldBe expectedItem.name
            orderItemEntity.price shouldBe expectedItem.price
            orderItemEntity.quantity shouldBe expectedItem.quantity
        }
    }
}

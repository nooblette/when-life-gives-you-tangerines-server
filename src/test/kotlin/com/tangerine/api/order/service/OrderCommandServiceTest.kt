package com.tangerine.api.order.service

import com.tangerine.api.order.command.PlaceOrderCommand
import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.entity.OrderItemEntity
import com.tangerine.api.order.fixture.command.createPlaceOrderCommand
import com.tangerine.api.order.fixture.domain.generator.TestOrderIdGenerator
import com.tangerine.api.order.fixture.domain.generator.TestOrderIdGenerator.Companion.STUB_ORDER_ID
import com.tangerine.api.order.repository.OrderItemRepository
import com.tangerine.api.order.repository.OrderRepository
import com.tangerine.api.order.result.PlaceOrderResult
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import
import org.springframework.transaction.annotation.Transactional

@SpringBootTest
@Import(TestOrderIdGenerator::class)
@Transactional
class OrderCommandServiceTest {
    @Autowired
    lateinit var orderCommandService: OrderCommandService

    @Autowired
    lateinit var orderRepository: OrderRepository

    @Autowired
    lateinit var orderItemRepository: OrderItemRepository

    lateinit var newOrder: PlaceOrderCommand

    @BeforeEach
    fun setUp() {
        newOrder = createPlaceOrderCommand()
    }

    @Test
    fun `주문 생성 성공 테스트`() {
        // when
        val result = orderCommandService.place(newOrder)

        // then
        result.shouldBeInstanceOf<PlaceOrderResult.Success>()
        result.orderId shouldBe STUB_ORDER_ID

        val actualOrder = orderRepository.findByOrderId(result.orderId)
        actualOrder shouldNotBe null
        actualOrder?.let {
            assertPlacementOrder(actualOrder)
            assertPlacementOrderItem(orderItemRepository.findByOrder(actualOrder))
        }
    }

    private fun assertPlacementOrder(actualOrder: OrderEntity) {
        actualOrder.status shouldBe OrderStatus.INIT // 주문 생성시 초기 상태는 INIT
        actualOrder.name shouldBe newOrder.customer.name
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

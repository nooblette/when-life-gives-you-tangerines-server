package com.tangerine.api.order.service

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
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.context.annotation.Import

@SpringBootTest
@Import(TestOrderIdGenerator::class)
class OrderCommandServiceTest {
    @Autowired
    private lateinit var orderCommandService: OrderCommandService

    @Autowired
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var orderItemRepository: OrderItemRepository

    private lateinit var newOrder: PlaceOrderCommand

    @BeforeEach
    fun setUp() {
        val orderItem =
            OrderItem(
                id = 1L,
                name = "테스트 주문 상품",
                price = 1000,
                quantity = 1,
            )
        newOrder = createPlaceOrderCommand(testOrderItems = listOf(orderItem))
    }

    @Test
    fun `주문 생성 성공 테스트`() {
        // when
        val result = orderCommandService.place(command = newOrder)

        // then
        result.shouldBeInstanceOf<PlaceOrderResult.Success>()
        result.orderId shouldBe STUB_ORDER_ID

        val actualOrder = orderRepository.findByOrderId(result.orderId)
        actualOrder.shouldNotBeNull()
        actualOrder.status shouldBe OrderStatus.INIT
        assertPlacementOrderDetail(actualOrder)
        assertPlacementOrderItem(orderItemRepository.findByOrder(actualOrder))
    }

    private fun assertPlacementOrderDetail(actualOrder: OrderEntity) {
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

        // 테스트 실행 순서에 따라 orderItem 엔티티의 Id 값이 달라져 테스트 격리 원칙에 위배
        // Id 생성은 DB 접근 계층(Jpa)의 책임이므로 비즈니스 로직에서 검증하지 않는다.
        actualOrderItem.forEachIndexed { index, orderItemEntity ->
            val expectedItem = newOrder.items[index]
            orderItemEntity.itemId shouldBe expectedItem.id
            orderItemEntity.name shouldBe expectedItem.name
            orderItemEntity.price shouldBe expectedItem.price
            orderItemEntity.quantity shouldBe expectedItem.quantity
        }
    }
}

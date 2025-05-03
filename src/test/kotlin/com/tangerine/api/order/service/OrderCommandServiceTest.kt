package com.tangerine.api.order.service

import com.tangerine.api.item.common.UnitType
import com.tangerine.api.item.entity.ItemEntity
import com.tangerine.api.item.mapper.toDomain
import com.tangerine.api.item.repository.ItemCommandRepository
import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.entity.OrderItemEntity
import com.tangerine.api.order.fixture.OrderItemInput
import com.tangerine.api.order.fixture.OrderItemInputs
import com.tangerine.api.order.fixture.TestOrderIdGenerator
import com.tangerine.api.order.fixture.TestOrderIdGenerator.Companion.STUB_ORDER_ID
import com.tangerine.api.order.fixture.createPlaceOrderCommand
import com.tangerine.api.order.mapper.toDomain
import com.tangerine.api.order.repository.OrderItemQueryRepository
import com.tangerine.api.order.repository.OrderQueryRepository
import com.tangerine.api.order.result.OrderPlacementResult
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
    lateinit var orderQueryRepository: OrderQueryRepository

    @Autowired
    lateinit var orderItemQueryRepository: OrderItemQueryRepository

    @Autowired
    lateinit var itemCommandRepository: ItemCommandRepository

    lateinit var newOrder: PlaceOrderCommand
    lateinit var newOrderItemInputs: OrderItemInputs

    @BeforeEach
    fun setUp() {
        val item1 =
            ItemEntity(
                name = "제주 노지 감귤 (10~15개입)",
                price = 12000,
                unit = UnitType.KG,
                quantity = 10,
            )

        val item2 =
            ItemEntity(
                name = "제주 노지 감귤 (10~15개입)",
                price = 12000,
                unit = UnitType.KG,
                quantity = 10,
            )

        itemCommandRepository.saveAll(listOf(item1, item2))
        newOrderItemInputs =
            OrderItemInputs(
                listOf(
                    OrderItemInput(item1.toDomain(), quantity = 2),
                    OrderItemInput(item2.toDomain(), quantity = 1),
                ),
            )
        newOrder = createPlaceOrderCommand(orderItemInputs = newOrderItemInputs)
    }

    @Test
    fun `주문 생성 성공 테스트`() {
        // when
        val result = orderCommandService.place(newOrder)

        // then
        result.shouldBeInstanceOf<OrderPlacementResult.Success>()
        result.orderId shouldBe STUB_ORDER_ID

        val placementOrder = orderQueryRepository.findByOrderId(result.orderId)
        placementOrder shouldNotBe null
        placementOrder?.let {
            assertPlacementOrder(placementOrder)
            assertPlacementOrderItem(orderItemQueryRepository.findByOrder(placementOrder))
        }
    }

    private fun assertPlacementOrder(placementOrder: OrderEntity) {
        placementOrder.status shouldBe OrderStatus.INIT // 주문 생성시 초기 상태는 INIT
        placementOrder.name shouldBe newOrder.customer.name
        placementOrder.recipient shouldBe newOrder.customer.recipient
        placementOrder.phone shouldBe newOrder.customer.phone
        placementOrder.address shouldBe newOrder.customer.address
        placementOrder.detailAddress shouldBe newOrder.customer.detailAddress
        placementOrder.zipCode shouldBe newOrder.customer.zipCode
        placementOrder.totalAmount shouldBe newOrder.totalAmount
    }

    private fun assertPlacementOrderItem(placementOrderItems: List<OrderItemEntity>) {
        placementOrderItems shouldNotBe null
        placementOrderItems shouldHaveSize newOrderItemInputs.size()
        placementOrderItems.map { it.toDomain() } shouldBe newOrderItemInputs.toOrderItems()
    }
}

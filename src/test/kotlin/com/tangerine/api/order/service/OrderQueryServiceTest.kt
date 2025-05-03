package com.tangerine.api.order.service

import com.tangerine.api.item.fixture.createTestItemEntity
import com.tangerine.api.item.repository.ItemCommandRepository
import com.tangerine.api.order.fixture.OrderItemInputs
import com.tangerine.api.order.fixture.TestOrderIdGenerator
import com.tangerine.api.order.fixture.createPlaceOrderCommand
import com.tangerine.api.order.mapper.toDomain
import com.tangerine.api.order.mapper.toDomains
import com.tangerine.api.order.mapper.toEntity
import com.tangerine.api.order.repository.OrderCommandRepository
import com.tangerine.api.order.repository.OrderItemCommandRepository
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest
import org.springframework.context.annotation.Import

@DataJpaTest
@Import(OrderQueryService::class)
class OrderQueryServiceTest {
    @Autowired
    private lateinit var orderQueryService: OrderQueryService

    @Autowired
    private lateinit var orderCommandRepository: OrderCommandRepository

    @Autowired
    private lateinit var orderItemCommandRepository: OrderItemCommandRepository

    @Autowired
    private lateinit var itemCommandRepository: ItemCommandRepository

    private lateinit var newOrderItemInputs: OrderItemInputs
    private lateinit var newOrder: PlaceOrderCommand

    @BeforeEach
    fun setUp() {
        val testItemEntities = createTestItemEntity(itemCommandRepository::saveAll)
        newOrderItemInputs =
            OrderItemInputs.createTestOrderItemInputs(
                quantityByIndex =
                    mapOf(
                        0 to 2,
                        1 to 1,
                    ),
                testItemEntities = testItemEntities,
            )
        newOrder = createPlaceOrderCommand(orderItemInputs = newOrderItemInputs)
    }

    @Test
    fun `요청받은 주문 Id에 해당하는 주문이 없으면 예외를 던진다`() {
        shouldThrow<IllegalArgumentException> {
            orderQueryService.getOrderById(TestOrderIdGenerator.STUB_ORDER_ID)
        }
    }

    @Test
    fun `주문 상세 내역 조회 성공 테스트`() {
        // given
        val orderID = TestOrderIdGenerator.STUB_ORDER_ID
        val orderEntity = orderCommandRepository.save(newOrder.toEntity(orderID))
        val orderItemEntities =
            orderItemCommandRepository.saveAll(newOrderItemInputs.toOrderItemEntity(order = orderEntity))
        val expectedOrder = orderEntity.toDomain(orderItemEntities.toDomains())

        // when
        val actualOrder = orderQueryService.getOrderById(orderID)

        // then
        actualOrder shouldBe expectedOrder
    }
}

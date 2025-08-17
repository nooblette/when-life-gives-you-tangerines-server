package com.tangerine.api.order.service

import com.tangerine.api.order.command.PlaceOrderCommand
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.entity.OrderItemEntity
import com.tangerine.api.order.fixture.domain.createPlaceOrderCommand
import com.tangerine.api.order.fixture.generator.TestOrderIdGenerator
import com.tangerine.api.order.mapper.toDomain
import com.tangerine.api.order.mapper.toDomains
import com.tangerine.api.order.mapper.toEntity
import com.tangerine.api.order.repository.OrderItemRepository
import com.tangerine.api.order.repository.OrderRepository
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
    private lateinit var orderRepository: OrderRepository

    @Autowired
    private lateinit var orderItemRepository: OrderItemRepository

    private lateinit var newOrder: PlaceOrderCommand

    @BeforeEach
    fun setUp() {
        newOrder = createPlaceOrderCommand()
    }

    @Test
    fun `요청받은 주문 Id에 해당하는 주문이 없으면 예외를 던진다`() {
        shouldThrow<IllegalArgumentException> {
            orderQueryService.getOrderByOrderId(TestOrderIdGenerator.STUB_ORDER_ID)
        }
    }

    @Test
    fun `주문 상세 내역 조회 성공 테스트`() {
        // given
        val orderID = TestOrderIdGenerator.STUB_ORDER_ID
        val orderEntity = orderRepository.save(newOrder.toEntity(orderID))
        val orderItemEntities = orderItemRepository.saveAll(newOrder.items.toEntities(orderEntity))
        val expectedOrder = orderEntity.toDomain(orderItemEntities.toDomains())

        // when
        val actualOrder = orderQueryService.getOrderByOrderId(orderID)

        // then
        actualOrder shouldBe expectedOrder
    }

    private fun List<OrderItem>.toEntities(orderEntity: OrderEntity): List<OrderItemEntity> =
        this.map { orderItem -> orderItem.toEntity(orderEntity) }
}

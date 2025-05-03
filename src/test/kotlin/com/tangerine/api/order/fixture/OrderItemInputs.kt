package com.tangerine.api.order.fixture

import com.tangerine.api.item.entity.ItemEntity
import com.tangerine.api.item.mapper.toDomain
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.entity.OrderItemEntity
import com.tangerine.api.order.mapper.toEntity

data class OrderItemInputs(
    val orderItemInputs: List<OrderItemInput>,
) {
    fun getTotalAmount(): Int = orderItemInputs.sumOf { it.item.price * it.quantity }

    fun toOrderItems(): List<OrderItem> = orderItemInputs.map { it.toOrderItem() }

    fun toOrderItemEntity(order: OrderEntity): List<OrderItemEntity> = toOrderItems().map { it.toEntity(order = order) }

    fun size() = orderItemInputs.size

    companion object {
        fun createTestOrderItemInputs(
            quantityByIndex: Map<Int, Int>,
            testItemEntities: List<ItemEntity>,
        ): OrderItemInputs =
            OrderItemInputs(
                testItemEntities.mapIndexed { index, itemEntity ->
                    val quantity = (
                        quantityByIndex[index]
                            ?: throw IllegalStateException("index=${index}에 대한 수량이 없습니다.")
                    )
                    OrderItemInput(
                        item = itemEntity.toDomain(),
                        quantity = quantity,
                    )
                },
            )
    }
}

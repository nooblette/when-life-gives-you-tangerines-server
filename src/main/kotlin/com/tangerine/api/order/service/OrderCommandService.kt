package com.tangerine.api.order.service

import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.domain.OrderIdGenerator
import com.tangerine.api.order.mapper.toEntity
import com.tangerine.api.order.repository.OrderCommandRepository
import com.tangerine.api.order.repository.OrderItemCommandRepository
import com.tangerine.api.order.result.OrderPlacementResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderCommandService(
    private val orderCommandRepository: OrderCommandRepository,
    private val orderItemCommandRepository: OrderItemCommandRepository,
    private val orderIdGenerator: OrderIdGenerator,
) {
    @Transactional
    fun place(placeOrderCommand: PlaceOrderCommand): OrderPlacementResult {
        val orderId = orderIdGenerator.generate()

        // 주문 생성
        val placedOrder = orderCommandRepository.save(placeOrderCommand.toEntity(orderId))

        // 주문 상품 생성
        orderItemCommandRepository.saveAll(placeOrderCommand.items.map { it.toEntity(placedOrder) })
        return OrderPlacementResult.Success(orderId = orderId)
    }

    @Transactional
    fun update(order: Order) {
        val existingOrder =
            orderCommandRepository
                .findById(order.id)
                .orElseThrow { IllegalArgumentException("주문이 존재하지 않습니다.") }

        // 이미 처리된 주문인 경우 상태를 변경하지 않는다.
        if (existingOrder.status == OrderStatus.DONE) {
            return
        }
        orderCommandRepository.save(order.toEntity())
    }
}

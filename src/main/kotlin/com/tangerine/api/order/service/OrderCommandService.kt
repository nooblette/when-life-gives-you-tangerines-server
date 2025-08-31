package com.tangerine.api.order.service

import com.tangerine.api.order.command.PlaceOrderCommand
import com.tangerine.api.order.domain.generator.OrderIdGenerator
import com.tangerine.api.order.mapper.toEntity
import com.tangerine.api.order.repository.OrderItemRepository
import com.tangerine.api.order.repository.OrderRepository
import com.tangerine.api.order.result.PlaceOrderResult
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class OrderCommandService(
    private val orderRepository: OrderRepository,
    private val orderItemRepository: OrderItemRepository,
    private val orderIdGenerator: OrderIdGenerator,
) {
    @Transactional
    fun createOrder(command: PlaceOrderCommand): PlaceOrderResult {
        val orderId = orderIdGenerator.generate()

        // 주문 생성
        val placedOrder = orderRepository.save(command.toEntity(orderId))

        // 주문 상품 생성
        orderItemRepository.saveAll(command.items.map { it.toEntity(placedOrder) })
        return PlaceOrderResult.Success(orderId = orderId)
    }
}

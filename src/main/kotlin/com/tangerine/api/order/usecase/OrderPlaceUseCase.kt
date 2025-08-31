package com.tangerine.api.order.usecase

import com.tangerine.api.item.result.DecreaseStockResult
import com.tangerine.api.item.service.ItemStockService
import com.tangerine.api.order.command.PlaceOrderCommand
import com.tangerine.api.order.result.PlaceOrderResult
import com.tangerine.api.order.service.OrderCommandService
import org.springframework.stereotype.Component
import org.springframework.transaction.annotation.Transactional

@Component
class OrderPlaceUseCase(
    private val itemStockService: ItemStockService,
    private val orderCommandService: OrderCommandService,
) {
    @Transactional
    fun place(command: PlaceOrderCommand): PlaceOrderResult =
        when (
            // 주문 수량만큼 재고 차감
            val decreaseStockResult = itemStockService.decreaseStockByOrderItems(command.items)
        ) {
            is DecreaseStockResult.Failure -> {
                PlaceOrderResult.Failure(reason = decreaseStockResult.message)
            }

            // 주문 생성
            is DecreaseStockResult.Success -> orderCommandService.createOrder(command)
        }
}

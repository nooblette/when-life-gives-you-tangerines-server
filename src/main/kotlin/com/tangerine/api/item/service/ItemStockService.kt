package com.tangerine.api.item.service

import com.tangerine.api.item.repository.ItemRepository
import com.tangerine.api.item.result.DecreaseStockResult
import com.tangerine.api.order.domain.OrderItem
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class ItemStockService(
    private val itemRepository: ItemRepository,
) {
    @Transactional
    fun decreaseStockByOrderItems(orderItems: List<OrderItem>): DecreaseStockResult {
        for (orderItem in orderItems) {
            val result = decreaseStock(orderItem)
            if (result is DecreaseStockResult.Failure) {
                return result
            }
        }

        return DecreaseStockResult.Success
    }

    private fun decreaseStock(orderItem: OrderItem): DecreaseStockResult {
        val itemEntity =
            itemRepository.findByIdWithPessimisticLock(orderItem.id)
                ?: throw IllegalArgumentException("잘못된 상품 Id(Id = ${orderItem.id}) 입니다.")

        if (orderItem.exceedsStock(itemEntity.stock)) {
            return DecreaseStockResult.Failure(message = generateFailureReason(itemEntity.stock))
        }

        itemEntity.stock -= orderItem.quantity
        return DecreaseStockResult.Success
    }

    private fun generateFailureReason(stock: Int) =
        if (stock == 0) {
            "품절 상품은 주문할 수 없습니다."
        } else {
            "남은 수량보다 많이 주문할 수 없습니다."
        }
}

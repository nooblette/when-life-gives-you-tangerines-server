package com.tangerine.api.order.policy

import com.tangerine.api.common.time.CurrentTimeProvider
import com.tangerine.api.order.common.OrderStatus
import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.result.EvaluateOrderPaymentResult
import org.springframework.stereotype.Component

@Component
class OrderPaymentApprovalPolicy(
    private val timeProvider: CurrentTimeProvider,
) {
    fun evaluate(
        order: Order,
        totalAmountForPayment: Int,
    ): EvaluateOrderPaymentResult =
        when {
            order.status == OrderStatus.IN_PROGRESS -> EvaluateOrderPaymentResult.InProgressOrder()
            order.status == OrderStatus.DONE -> EvaluateOrderPaymentResult.CompletedOrder()
            order.isExpired() -> EvaluateOrderPaymentResult.ExpiredOrder()
            order.misMatches(totalAmountForPayment) -> EvaluateOrderPaymentResult.MisMatchedTotalAmount()
            else -> EvaluateOrderPaymentResult.Success()
        }

    private fun Order.isExpired() = this.status == OrderStatus.EXPIRED || this.isExpired(timeProvider.now())
}

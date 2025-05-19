package com.tangerine.api.order.policy

import com.tangerine.api.common.time.CurrentTimeProvider
import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.result.OrderPaymentEvaluationResult
import org.springframework.stereotype.Component

@Component
class OrderPaymentApprovalPolicy(
    private val timeProvider: CurrentTimeProvider,
) {
    fun evaluate(
        order: Order,
        totalAmountForPayment: Int,
    ): OrderPaymentEvaluationResult =
        when {
            order.isNotInit -> OrderPaymentEvaluationResult.AlreadyInProgressOrder()
            order.isExpired(timeProvider.now()) -> OrderPaymentEvaluationResult.ExpiredOrder()
            order.misMatches(totalAmountForPayment) -> OrderPaymentEvaluationResult.MisMatchedTotalAmount()
            else -> OrderPaymentEvaluationResult.Success()
        }
}

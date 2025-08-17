package com.tangerine.api.order.result

sealed interface PlaceOrderResult {
    data class Success(
        val orderId: String,
    ) : PlaceOrderResult

    data class Failure(
        val reason: String,
    ) : PlaceOrderResult
}

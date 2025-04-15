package com.tangerine.api.order.result

sealed class OrderPlacementResult {
    data class Success(
        val orderId: String,
    ) : OrderPlacementResult()

    data class Failure(
        val reason: String,
    ) : OrderPlacementResult()
}

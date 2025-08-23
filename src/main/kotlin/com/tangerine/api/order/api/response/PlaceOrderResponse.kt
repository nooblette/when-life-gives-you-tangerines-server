package com.tangerine.api.order.api.response

sealed interface PlaceOrderResponse {
    data class Success(
        val orderId: String,
    ) : PlaceOrderResponse

    data class Failure(
        val reason: String,
    ) : PlaceOrderResponse
}

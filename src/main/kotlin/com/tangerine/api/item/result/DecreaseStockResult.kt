package com.tangerine.api.item.result

sealed interface DecreaseStockResult {
    data class Failure(
        val message: String,
    ) : DecreaseStockResult

    data object Success : DecreaseStockResult
}

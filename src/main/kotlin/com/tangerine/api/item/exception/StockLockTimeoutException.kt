package com.tangerine.api.item.exception

import com.tangerine.api.common.exception.ResourceConflictException

class StockLockTimeoutException(
    override val message: String = "상품 주문 요청이 많습니다. 잠시 후 다시 시도해주세요.",
) : ResourceConflictException(message = message)

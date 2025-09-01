package com.tangerine.api.item.exception

import com.tangerine.api.common.exception.ResourceConflictException

class StockConflictException(
    override val message: String,
) : ResourceConflictException(message = message)

package com.tangerine.api.common.exception

import com.tangerine.api.global.response.ErrorCodes

class UnauthorizedException(
    override val message: String,
    val code: String = ErrorCodes.UNAUTHORIZED,
) : RuntimeException(message)

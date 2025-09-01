package com.tangerine.api.common.exception

import com.tangerine.api.global.response.ErrorCodes

open class ResourceConflictException(
    override val message: String,
    val code: String = ErrorCodes.CONFLICT,
) : RuntimeException(message)

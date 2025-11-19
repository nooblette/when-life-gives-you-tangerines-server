package com.tangerine.api.common.exception

import com.tangerine.api.global.response.ErrorCodes

class RateLimitExceededException(
    override val message: String = "호출 제한 횟수를 초과했습니다. 잠시 후 다시 시도해주세요",
    val code: String = ErrorCodes.TOO_MANY_REQUESTS,
) : RuntimeException(message)

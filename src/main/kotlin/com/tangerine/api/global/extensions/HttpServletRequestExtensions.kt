package com.tangerine.api.global.extensions

import jakarta.servlet.http.HttpServletRequest

fun HttpServletRequest.getMySessionId(): String =
    this.cookies
        ?.firstOrNull { it.name == "sessionId" }
        ?.value
        ?: throw IllegalArgumentException("유효하지 않은 세션입니다.")

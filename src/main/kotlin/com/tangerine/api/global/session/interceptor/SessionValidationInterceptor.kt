package com.tangerine.api.global.session.interceptor

import com.tangerine.api.common.exception.UnauthorizedException
import com.tangerine.api.global.session.manager.SessionManager
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class SessionValidationInterceptor(
    private val sessionManager: SessionManager,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (request.method == "OPTIONS") {
            return true // Preflight는 검증 없이 통과
        }

        val sessionId =
            request.cookies
                ?.firstOrNull { it.name == "sessionId" }
                ?.value
                ?: throw UnauthorizedException("유효하지 않은 세션입니다.")

        sessionManager.validateAndExtendSession(sessionId)

        return true
    }
}

package com.tangerine.api.global.ratelimit.interceptor

import com.tangerine.api.global.extensions.getMySessionId
import com.tangerine.api.global.ratelimit.RateLimiter
import com.tangerine.api.global.ratelimit.enum.RateLimitType
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor
import java.time.Duration

@Component
class OrderRateLimitInterceptor(
    private val rateLimiter: RateLimiter,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        // Preflight 요청은 검증 없이 통과
        if (request.method == "OPTIONS") {
            return true
        }

        return rateLimiter.tryAcquire(
            type = RateLimitType.SESSION,
            key = request.getMySessionId(),
            limit = LIMIT,
            duration = Duration.ofMinutes(DURATION),
        )
    }

    companion object {
        const val LIMIT = 100
        const val DURATION = 1L
    }
}

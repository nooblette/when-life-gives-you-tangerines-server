package com.tangerine.api.global.session.manager

import com.tangerine.api.common.exception.UnauthorizedException
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration
import java.util.UUID

@Component
class SessionManager(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    fun createSession(): String {
        val sessionId = UUID.randomUUID().toString()

        // 세션 저장 (빈 값 또는 초기 데이터)
        redisTemplate.opsForValue().set(
            SESSION_PREFIX + sessionId,
            VALUE,
            Duration.ofMinutes(SESSION_EXPIRATION_MINUTES),
        )
        return sessionId
    }

    fun validateAndExtendSession(sessionId: String) {
        val key = SESSION_PREFIX + sessionId
        val exists = redisTemplate.hasKey(key)

        if (!exists) {
            throw UnauthorizedException("유효하지 않은 세션 Id입니다.")
        }
        redisTemplate.expire(key, Duration.ofMinutes(SESSION_EXPIRATION_MINUTES))
    }

    companion object {
        private const val SESSION_PREFIX = "session:"
        private const val SESSION_EXPIRATION_MINUTES = 30L
        private const val VALUE = "1"
    }
}

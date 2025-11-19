package com.tangerine.api.global.ratelimit

import com.tangerine.api.common.exception.RateLimitExceededException
import com.tangerine.api.global.ratelimit.enum.RateLimitType
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Component
import java.time.Duration

@Component
class RateLimiter(
    private val redisTemplate: RedisTemplate<String, String>,
) {
    fun tryAcquire(
        type: RateLimitType,
        key: String,
        limit: Int,
        duration: Duration,
    ): Boolean {
        val redisKey = "$RATE_LIMIT_PREFIX:$type:$key"
        val newCount =
            redisTemplate.opsForValue().increment(redisKey)
                ?: return false

        if (newCount >= limit) {
            throw RateLimitExceededException()
        }

        // 최초 요청인 경우 TTL 설정
        return (newCount <= limit).also {
            if (newCount == 1L) {
                redisTemplate.expire(redisKey, duration)
            }
        }
    }

    companion object {
        private const val RATE_LIMIT_PREFIX = "rate_limit:"
    }
}

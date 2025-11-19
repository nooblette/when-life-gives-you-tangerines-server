package com.tangerine.api.global.ratelimit.enum

enum class RateLimitType(
    private val type: String,
) {
    SESSION("session"),
    ;

    override fun toString(): String = type
}

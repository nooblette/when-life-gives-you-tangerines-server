package com.tangerine.api.common.time

import java.time.LocalDateTime

interface CurrentTimeProvider {
    fun now(): LocalDateTime
}

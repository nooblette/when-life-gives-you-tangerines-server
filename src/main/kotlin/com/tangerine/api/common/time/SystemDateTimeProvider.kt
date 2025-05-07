package com.tangerine.api.common.time

import org.springframework.stereotype.Component
import java.time.LocalDateTime

@Component
class SystemDateTimeProvider : CurrentTimeProvider {
    override fun now(): LocalDateTime = LocalDateTime.now()
}

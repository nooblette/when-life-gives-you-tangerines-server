package com.tangerine.api.order.domain.generator

import org.springframework.stereotype.Component
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Component
class DefaultOrderIdGenerator : OrderIdGenerator {
    override fun generate(): String {
        val timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss"))
        val random =
            (1..6)
                .map { ('A'..'Z') + ('0'..'9') }
                .flatten()
                .shuffled()
                .take(6)
                .joinToString("")
        return "$timestamp-$random"
    }
}

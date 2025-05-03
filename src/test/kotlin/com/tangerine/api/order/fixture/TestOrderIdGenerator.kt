package com.tangerine.api.order.fixture

import com.tangerine.api.order.domain.OrderIdGenerator
import org.springframework.boot.test.context.TestConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary

@TestConfiguration
class TestOrderIdGenerator {
    @Bean
    @Primary
    fun stubOrderIdGenerator(): OrderIdGenerator =
        object : OrderIdGenerator {
            override fun generate(): String = STUB_ORDER_ID
        }

    companion object {
        const val STUB_ORDER_ID = "TEST-ORDER-123"
    }
}

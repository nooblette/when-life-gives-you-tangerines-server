package com.tangerine.api.order.domain.generator

interface OrderIdGenerator {
    fun generate(): String
}

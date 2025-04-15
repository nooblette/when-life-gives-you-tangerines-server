package com.tangerine.api.order.domain

interface OrderIdGenerator {
    fun generate(): String
}

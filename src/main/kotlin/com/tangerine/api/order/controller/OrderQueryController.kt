package com.tangerine.api.order.controller

import com.tangerine.api.order.api.response.OrderResponse
import com.tangerine.api.order.mapper.toResponse
import com.tangerine.api.order.service.OrderQueryService
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/order")
class OrderQueryController(
    private val orderQueryService: OrderQueryService,
) {
    @GetMapping("/{orderId}")
    fun getOrderById(
        @PathVariable orderId: String,
    ): OrderResponse = orderQueryService.getOrderByOrderId(orderId).toResponse()
}

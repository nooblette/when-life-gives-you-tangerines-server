package com.tangerine.api.order.controller

import com.tangerine.api.order.api.request.PlaceOrderRequest
import com.tangerine.api.order.mapper.toPlaceOrderCommand
import com.tangerine.api.order.result.PlaceOrderResult
import com.tangerine.api.order.service.OrderCommandService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderCommandController(
    private val orderCommandService: OrderCommandService,
) {
    @PostMapping
    fun createOrderCommand(
        @Valid @RequestBody request: PlaceOrderRequest,
    ): PlaceOrderResult = orderCommandService.place(request.toPlaceOrderCommand())
}

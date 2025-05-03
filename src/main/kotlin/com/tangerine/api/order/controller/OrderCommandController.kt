package com.tangerine.api.order.controller

import com.tangerine.api.order.api.request.OrderRequest
import com.tangerine.api.order.mapper.toPlaceOrderCommand
import com.tangerine.api.order.service.OrderCommandService
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/order")
class OrderCommandController(
    private val orderCommandService: OrderCommandService,
) {
    @PostMapping
    fun createOrderCommand(
        @Valid @RequestBody orderRequest: OrderRequest,
    ) = orderCommandService.place(orderRequest.toPlaceOrderCommand())
}

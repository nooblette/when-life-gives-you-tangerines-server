package com.tangerine.api.order.controller

import com.tangerine.api.order.api.request.PlaceOrderRequest
import com.tangerine.api.order.api.response.PlaceOrderResponse
import com.tangerine.api.order.mapper.toPlaceOrderCommand
import com.tangerine.api.order.mapper.toResponse
import com.tangerine.api.order.usecase.PlaceOrderUseCase
import jakarta.validation.Valid
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderCommandController(
    private val placeOrderUseCase: PlaceOrderUseCase,
) {
    @PostMapping
    fun createOrderCommand(
        @Valid @RequestBody request: PlaceOrderRequest,
    ): PlaceOrderResponse =
        placeOrderUseCase
            .place(request.toPlaceOrderCommand())
            .toResponse()
}

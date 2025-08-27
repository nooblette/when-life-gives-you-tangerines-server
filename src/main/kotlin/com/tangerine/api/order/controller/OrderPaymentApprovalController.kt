package com.tangerine.api.order.controller

import com.tangerine.api.order.api.request.ApproveOrderPaymentRequest
import com.tangerine.api.order.api.response.ApproveOrderPaymentResponse
import com.tangerine.api.order.mapper.toApproveOrderPaymentCommand
import com.tangerine.api.order.mapper.toResponse
import com.tangerine.api.order.usecase.ApproveOrderPaymentUseCase
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/orders")
class OrderPaymentApprovalController(
    private val approveOrderPaymentUseCase: ApproveOrderPaymentUseCase,
) {
    @PostMapping("/{orderId}/payments")
    fun approve(
        @PathVariable orderId: String,
        @RequestBody @Valid request: ApproveOrderPaymentRequest,
    ): ResponseEntity<ApproveOrderPaymentResponse> {
        val response =
            approveOrderPaymentUseCase
                .approve(request.toApproveOrderPaymentCommand(orderId = orderId))
                .toResponse()

        return when (response) {
            is ApproveOrderPaymentResponse.Success -> ResponseEntity.ok(response)
            is ApproveOrderPaymentResponse.Failure -> response.toResponseEntity()
        }
    }
}

package com.tangerine.api.order.controller

import com.tangerine.api.global.response.ApiResult
import com.tangerine.api.global.response.Error
import com.tangerine.api.global.response.Success
import com.tangerine.api.global.response.toResponseEntity
import com.tangerine.api.order.api.request.OrderPaymentApprovalRequest
import com.tangerine.api.order.mapper.toCommand
import com.tangerine.api.order.result.OrderPaymentApprovalResult
import com.tangerine.api.order.service.OrderPaymentApprovalService
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
    private val orderPaymentApprovalService: OrderPaymentApprovalService,
) {
    @PostMapping("/{orderId}/payment-approval")
    fun approve(
        @PathVariable orderId: String,
        @RequestBody @Valid orderPaymentApprovalRequest: OrderPaymentApprovalRequest,
    ): ResponseEntity<out ApiResult<String>> =
        orderPaymentApprovalService
            .approve(orderPaymentApprovalRequest.toCommand(orderId = orderId))
            .toApiResult()
            .toResponseEntity()

    private fun OrderPaymentApprovalResult.toApiResult(): ApiResult<String> =
        when (this) {
            is OrderPaymentApprovalResult.Failure -> Error(message = this.message, code = this.code)
            is OrderPaymentApprovalResult.Success -> Success(data = this.message)
        }
}

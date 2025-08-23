package com.tangerine.api.order.mapper

import com.tangerine.api.order.api.request.CustomerRequest
import com.tangerine.api.order.api.request.OrderItemRequest
import com.tangerine.api.order.api.request.PlaceOrderRequest
import com.tangerine.api.order.api.response.CustomerResponse
import com.tangerine.api.order.api.response.OrderItemResponse
import com.tangerine.api.order.api.response.OrderResponse
import com.tangerine.api.order.api.response.PlaceOrderResponse
import com.tangerine.api.order.command.PlaceOrderCommand
import com.tangerine.api.order.domain.Customer
import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.result.PlaceOrderResult

fun PlaceOrderRequest.toPlaceOrderCommand(): PlaceOrderCommand =
    PlaceOrderCommand(
        customer = customer.toDomain(),
        items = items.map(OrderItemRequest::toDomain),
        totalAmount = totalAmount,
    )

fun CustomerRequest.toDomain(): Customer =
    Customer(
        name = this.name,
        recipient = this.recipient,
        phone = this.phone,
        address = this.address,
        detailAddress = this.detailAddress,
        zipCode = this.zipCode,
    )

fun OrderItemRequest.toDomain(): OrderItem =
    OrderItem(
        id = this.id,
        name = this.name,
        price = this.price,
        quantity = this.quantity,
    )

fun Order.toResponse(): OrderResponse =
    OrderResponse(
        orderId = this.orderId,
        customer = this.customer.toResponse(),
        items = this.items.toResponses(),
        totalAmount = this.totalAmount,
    )

fun Customer.toResponse(): CustomerResponse =
    CustomerResponse(
        name = this.name,
        recipient = this.recipient,
        phone = this.phone,
        address = this.address,
        detailAddress = this.detailAddress,
        zipCode = this.zipCode,
    )

fun List<OrderItem>.toResponses(): List<OrderItemResponse> = this.map { it.toResponse() }

private fun OrderItem.toResponse(): OrderItemResponse =
    OrderItemResponse(
        id = this.id,
        name = this.name,
        price = this.price,
        quantity = this.quantity,
    )

fun PlaceOrderResult.toResponse(): PlaceOrderResponse =
    when (this) {
        is PlaceOrderResult.Success -> PlaceOrderResponse.Success(orderId = this.orderId)
        is PlaceOrderResult.Failure -> PlaceOrderResponse.Failure(reason = this.reason)
    }

package com.tangerine.api.order.mapper

import com.tangerine.api.order.api.request.CustomerRequest
import com.tangerine.api.order.api.request.OrderItemRequest
import com.tangerine.api.order.api.request.OrderRequest
import com.tangerine.api.order.domain.Customer
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.service.PlaceOrderCommand

fun OrderRequest.toPlaceOrderCommand(): PlaceOrderCommand =
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

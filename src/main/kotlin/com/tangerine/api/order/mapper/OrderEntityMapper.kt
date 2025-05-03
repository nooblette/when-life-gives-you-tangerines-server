package com.tangerine.api.order.mapper

import com.tangerine.api.order.domain.Customer
import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.entity.OrderItemEntity
import com.tangerine.api.order.service.PlaceOrderCommand

fun PlaceOrderCommand.toEntity(orderId: String) =
    OrderEntity(
        orderId = orderId,
        name = customer.name,
        recipient = customer.recipient,
        phone = customer.phone,
        address = customer.address,
        detailAddress = customer.detailAddress,
        zipCode = customer.zipCode,
        totalAmount = totalAmount,
    )

fun OrderItem.toEntity(order: OrderEntity) =
    OrderItemEntity(
        itemId = id,
        name = name,
        price = price,
        quantity = quantity,
        order = order,
    )

fun OrderEntity.toDomain(items: List<OrderItem>) =
    Order(
        id = requireNotNull(id) { "주문 Id가 없습니다. " },
        orderId = orderId,
        customer =
            Customer.purchaser(
                name = name,
                recipient = recipient,
                phone = phone,
                address = address,
                detailAddress = detailAddress,
                zipCode = zipCode,
            ),
        items = items,
        totalAmount = totalAmount,
        status = status,
    )

fun List<OrderItemEntity>.toDomains() = this.map { it.toDomain() }

fun OrderItemEntity.toDomain() =
    OrderItem(
        id = requireNotNull(id) { "주문 상품의 상품 Id가 없습니다." },
        name = name,
        price = price,
        quantity = quantity,
    )

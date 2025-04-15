package com.tangerine.api.order.mapper

import com.tangerine.api.order.domain.Order
import com.tangerine.api.order.domain.OrderItem
import com.tangerine.api.order.entity.OrderEntity
import com.tangerine.api.order.entity.OrderItemEntity

fun Order.toEntity(orderId: String) =
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

fun OrderItemEntity.toDomain() =
    OrderItem(
        id = id ?: throw IllegalArgumentException("OrderItem 엔티티의 Id가 없습니다."),
        name = name,
        price = price,
        quantity = quantity,
    )

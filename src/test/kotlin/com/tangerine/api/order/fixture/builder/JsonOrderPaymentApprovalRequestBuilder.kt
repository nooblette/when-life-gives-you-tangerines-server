package com.tangerine.api.order.fixture.builder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class JsonOrderPaymentApprovalRequestBuilder {
    private val om: ObjectMapper = jacksonObjectMapper()
    private val root: ObjectNode = om.createObjectNode()

    // 필수 주문 결제 요청 객체 생성
    fun withDefaultOrderPaymentApprovalRequest() =
        apply {
            root.put("paymentKey", "TEST")
            root.put("totalAmount", 32000)
        }

    fun withoutPaymentKey() =
        apply {
            root.remove("paymentKey")
        }

    fun withoutTotalAmount() =
        apply {
            root.remove("totalAmount")
        }

    fun withMinusTotalAmount() =
        apply {
            root.put("totalAmount", 32000 * -1)
        }

    fun build(): String = om.writeValueAsString(root)
}

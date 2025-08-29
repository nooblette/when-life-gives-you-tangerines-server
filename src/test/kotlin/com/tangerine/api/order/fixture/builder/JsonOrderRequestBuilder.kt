package com.tangerine.api.order.fixture.builder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.node.ArrayNode
import com.fasterxml.jackson.databind.node.ObjectNode
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper

class JsonOrderRequestBuilder {
    private val om: ObjectMapper = jacksonObjectMapper()
    private val root: ObjectNode = om.createObjectNode()

    // 필수 customer 객체 생성
    fun withDefaultCustomer() =
        apply {
            root.set<ObjectNode>(
                "customer",
                om.createObjectNode().apply {
                    put("name", "홍길동")
                    put("recipient", "이순신")
                    put("phone", "010-1234-5678")
                    put("address", "서울시 강남구 테헤란로 123")
                    put("detailAddress", null as String?)
                    put("zipCode", "01234")
                },
            )
        }

    // 필수 items 배열 생성
    fun withDefaultItems() =
        apply {
            val arr: ArrayNode = om.createArrayNode()
            arr.addObject().apply {
                put("id", 1L)
                put("name", "제주 노지 감귤")
                put("price", 12000)
                put("quantity", 2)
            }
            arr.addObject().apply {
                put("id", 2L)
                put("name", "제주 하우스 감귤")
                put("price", 15000)
                put("quantity", 1)
            }
            root.set<ArrayNode>("items", arr)
        }

    // 필수 totalAmount 세팅
    fun withDefaultTotalAmount() =
        apply {
            root.put("totalAmount", 39000)
        }

    // 필수 orderName 세팅
    fun withDefaultOrderName() =
        apply {
            root.put("orderName", "테스트 주문")
        }

    // customer 키 전체를 제거
    fun withoutCustomer() =
        apply {
            root.remove("customer")
        }

    // customer 하위 name 필드만 제거
    fun withoutCustomerField(fieldName: String) =
        apply {
            (root.get("customer") as? ObjectNode)?.remove(fieldName)
        }

    // items 키 전체를 제거
    fun withoutItems() =
        apply {
            root.remove("items")
        }

    // items 배열의 특정 인덱스 객체에서 price 필드만 제거
    fun withoutItemFieldAt(
        index: Int,
        fieldName: String,
    ) = apply {
        (root.get("items") as? ArrayNode)
            ?.get(index)
            ?.let { it as? ObjectNode }
            ?.remove(fieldName)
    }

    // totalAmount 키를 제거
    fun withoutTotalAmount() =
        apply {
            root.remove("totalAmount")
        }

    // 최종 JSON 문자열 반환
    fun build(): String = om.writeValueAsString(root)
}

package com.tangerine.api.order.command

data class ApproveOrderPaymentCommand(
    val orderId: String,
    val paymentKey: String,
    val totalAmount: Int,
) {
    init {
        require(totalAmount > 0) { "주문금액은 음수(-)일 수 없습니다." }
    }
}

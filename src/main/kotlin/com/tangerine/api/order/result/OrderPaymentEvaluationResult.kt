package com.tangerine.api.order.result

sealed class OrderPaymentEvaluationResult(
    open val message: String,
    open val isSuccess: Boolean = false,
    open val isFailure: Boolean = false,
) {
    data class Success(
        override val message: String = "Ok",
        override val isSuccess: Boolean = true,
    ) : OrderPaymentEvaluationResult(message = message)

    open class Failure(
        override val message: String,
        override val isFailure: Boolean = true,
        open val code: String,
    ) : OrderPaymentEvaluationResult(message = message)

    data class InProgressOrder(
        override val message: String = "결제 진행 중인 주문입니다.",
        override val code: String = "ALREADY_IN_PROGRESS_ORDER",
    ) : Failure(message = message, code = code)

    data class CompletedOrder(
        override val message: String = "완료된 주문입니다.",
        override val code: String = "COMPLETED_ORDER",
    ) : Failure(message = message, code = code)

    data class ExpiredOrder(
        override val message: String = "만료된 주문입니다.",
        override val code: String = "EXPIRED_ORDER",
    ) : Failure(message = message, code = code)

    data class MisMatchedTotalAmount(
        override val message: String = "총 주문 금액이 불일치합니다.",
        override val code: String = "MIS_MATCHED_TOTAL_AMOUNT",
    ) : Failure(message = message, code = code)
}

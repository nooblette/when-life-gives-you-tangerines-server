package com.tangerine.api.payment.client.toss.exception

import org.springframework.http.HttpStatus

open class TossPaymentException(
    open val httpStatus: HttpStatus,
    open val code: String,
    override val message: String,
) : RuntimeException(message) {
    class UnauthorizedKey(
        override val httpStatus: HttpStatus = HttpStatus.UNAUTHORIZED,
        override val code: String = "UNAUTHORIZED_KEY",
        override val message: String = "인증되지 않은 시크릿 키 혹은 클라이언트 키 입니다.",
    ) : TossPaymentException(httpStatus, code, message)

    class EmptyBody(
        override val httpStatus: HttpStatus,
        override val code: String = "UNKNOWN_ERROR",
        override val message: String,
    ) : TossPaymentException(httpStatus, code, message)

    class InvalidJsonResponse(
        override val httpStatus: HttpStatus,
        override val code: String = "INVALID_JSON_RESPONSE",
        override val message: String,
    ) : TossPaymentException(httpStatus, code, message)
}

package com.tangerine.api.payment.client.toss.exception

import org.springframework.http.HttpStatus

open class TossPaymentException(
    open val httpStatus: HttpStatus,
    open val code: String,
    override val message: String,
) : RuntimeException(message) {
    companion object {
        fun unauthorizedKey(): TossPaymentException =
            TossPaymentException(
                httpStatus = HttpStatus.UNAUTHORIZED,
                code = "UNAUTHORIZED_KEY",
                message = "인증되지 않은 시크릿 키 혹은 클라이언트 키 입니다.",
            )

        fun emptyBody(httpStatus: HttpStatus): TossPaymentException =
            TossPaymentException(
                httpStatus = httpStatus,
                code = "UNKNOWN_ERROR",
                message = "응답 본문이 비어있습니다.",
            )

        fun invalidJsonResponse(
            httpStatus: HttpStatus,
            message: String,
        ): TossPaymentException =
            TossPaymentException(
                httpStatus = httpStatus,
                code = "INVALID_JSON_RESPONSE",
                message = message,
            )
    }
}

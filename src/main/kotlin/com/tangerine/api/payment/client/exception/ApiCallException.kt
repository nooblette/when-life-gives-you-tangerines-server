package com.tangerine.api.payment.client.exception

import org.springframework.http.HttpStatus

class ApiCallException(
    val httpStatus: HttpStatus,
    // API 호출 정보
    methodKey: String? = null,
    // 서버 응답 에러 메시지
    responseBody: String? = null,
) : RuntimeException(buildMessage(httpStatus, methodKey, responseBody)) {
    companion object {
        fun buildMessage(
            httpStatus: HttpStatus,
            methodKey: String? = null,
            responseBody: String? = null,
        ): String =
            buildString {
                append("API call failed")
                methodKey?.let { append(" [$it]") }
                append(" (${httpStatus.value()} ${httpStatus.reasonPhrase})")
                responseBody?.let { append(": $it") }
            }
    }
}

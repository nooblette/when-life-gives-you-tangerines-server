package com.tangerine.api.common.feign.decoder

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.tangerine.api.payment.client.exception.ApiCallException
import com.tangerine.api.payment.client.toss.exception.TossPaymentException
import com.tangerine.api.payment.client.toss.response.TossErrorResponse
import feign.Response
import feign.Util
import feign.codec.ErrorDecoder
import org.springframework.http.HttpStatus
import java.lang.Exception
import java.nio.charset.StandardCharsets

/**
 * FeignClient Error Decoder
 * - 토스페이먼츠 API 에러 응답 해석 및 TossPaymentException 예외로 래핑
 */
class TossPaymentErrorDecoder(
    private val objectMapper: ObjectMapper = jacksonObjectMapper(),
) : ErrorDecoder {
    override fun decode(
        methodKey: String,
        response: Response,
    ): Exception {
        val httpStatus = HttpStatus.valueOf(response.status())
        val responseBody =
            response.body()?.let {
                Util.toString(it.asReader(StandardCharsets.UTF_8))
            }

        return when {
            httpStatus == HttpStatus.UNAUTHORIZED -> TossPaymentException.unauthorizedKey()
            httpStatus.isError -> errorHandling(httpStatus = httpStatus, responseBody = responseBody)
            else -> ApiCallException(httpStatus = httpStatus, methodKey = methodKey, responseBody = responseBody)
        }
    }

    private fun errorHandling(
        httpStatus: HttpStatus,
        responseBody: String?,
    ) = runCatching {
        val tossError = objectMapper.readValue(responseBody, TossErrorResponse::class.java)
        TossPaymentException(
            httpStatus = httpStatus,
            code = tossError.code,
            message = tossError.message,
        )
    }.getOrElse {
        when {
            responseBody.isNullOrBlank() ->
                TossPaymentException.emptyBody(httpStatus = httpStatus)

            else ->
                TossPaymentException.invalidJsonResponse(
                    httpStatus = httpStatus,
                    message = responseBody,
                )
        }
    }
}

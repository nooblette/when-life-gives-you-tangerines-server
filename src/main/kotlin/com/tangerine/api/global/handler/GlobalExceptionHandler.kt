package com.tangerine.api.global.handler

import com.fasterxml.jackson.databind.JsonMappingException
import com.tangerine.api.common.exception.ResourceConflictException
import com.tangerine.api.common.exception.UnauthorizedException
import com.tangerine.api.global.response.ApiResponse
import com.tangerine.api.global.response.ErrorCodes
import com.tangerine.api.order.exception.OrderAlreadyInProgressException
import com.tangerine.api.order.result.EvaluateOrderPaymentResult.InProgressOrder
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(exception: MethodArgumentNotValidException): ResponseEntity<ApiResponse.Error> {
        val errors =
            exception.bindingResult.fieldErrors.map { fieldError ->
                ApiResponse.ValidationError(
                    field = fieldError.field,
                    message = fieldError.defaultMessage ?: "Invalid value",
                )
            }

        return ResponseEntity(
            ApiResponse.ValidationErrorResponse(
                message = "유효성 검사에 실패했습니다.",
                code = ErrorCodes.INVALID_ARGUMENT,
                errors = errors,
            ),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleHttpMessageNotReadable(exception: HttpMessageNotReadableException): ResponseEntity<ApiResponse.ValidationErrorResponse> {
        // 내부 원인이 JsonMappingException 예외인 경우만 처리
        // 필수 파라미터 누락으로 코틀린 객체 생성 실패시 발생하는 MissingKotlinParameterException 예외도 JsonMappingException 예외를 상속
        val mappingEx = exception.cause as? JsonMappingException

        // JsonMappingException.path 에 쌓인 Reference 를 full path 로 변환
        val fieldName =
            mappingEx
                ?.path
                ?.let { toFieldName(it) }
                ?: "unknown"

        // 누락된 필드로 에러 응답1
        val error =
            ApiResponse.ValidationError(
                field = fieldName,
                message = "${fieldName}는 필수 값입니다.",
            )

        return ResponseEntity(
            ApiResponse.ValidationErrorResponse(
                message = "필수 요청 파라미터가 누락되었습니다.",
                code = ErrorCodes.MISSING_FIELD,
                errors = listOf(error),
            ),
            HttpStatus.BAD_REQUEST,
        )
    }

    private fun toFieldName(path: List<JsonMappingException.Reference>): String {
        val sb = StringBuilder()
        path.forEach { reference ->
            if (reference.fieldName != null) {
                if (sb.isNotEmpty()) sb.append('.')
                sb.append(reference.fieldName)
            } else {
                sb
                    .append('[')
                    .append(reference.index)
                    .append(']')
            }
        }
        return sb.toString()
    }

    @ExceptionHandler(IllegalArgumentException::class)
    fun handlerIllegalArgumentException(exception: IllegalArgumentException): ResponseEntity<ApiResponse.Error> =
        ResponseEntity(
            ApiResponse.Error(
                message = exception.message ?: "잘못된 요청입니다.",
                code = ErrorCodes.INVALID_ARGUMENT,
            ),
            HttpStatus.BAD_REQUEST,
        )

    @ExceptionHandler(ResourceConflictException::class)
    fun handleResourceConflictException(exception: ResourceConflictException): ResponseEntity<ApiResponse.Error> =
        ResponseEntity(
            createConflictResponseBody(exception = exception),
            HttpStatus.CONFLICT,
        )

    private fun createConflictResponseBody(exception: ResourceConflictException) =
        when (exception) {
            is OrderAlreadyInProgressException ->
                ApiResponse.Error(
                    message = InProgressOrder().message,
                    code = InProgressOrder().code,
                )

            else -> {
                ApiResponse.Error(
                    message = exception.message,
                    code = exception.code,
                )
            }
        }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorizedException(exception: UnauthorizedException): ResponseEntity<ApiResponse.Error> =
        ResponseEntity(
            ApiResponse.Error(
                message = exception.message,
                code = exception.code,
            ),
            HttpStatus.UNAUTHORIZED,
        )
}

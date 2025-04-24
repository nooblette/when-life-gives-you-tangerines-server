package com.tangerine.api.global.handler

import com.tangerine.api.global.response.ApiResult
import com.tangerine.api.global.response.ErrorCodes
import com.tangerine.api.global.response.ValidationError
import com.tangerine.api.global.response.ValidationErrorResponse
import com.tangerine.api.order.exception.MissingRequestFieldException
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(exception: MethodArgumentNotValidException): ResponseEntity<ApiResult<Nothing>> {
        val errors =
            exception.bindingResult.fieldErrors.map { fieldError ->
                ValidationError(
                    field = fieldError.field,
                    message = fieldError.defaultMessage ?: "Invalid value",
                )
            }

        return ResponseEntity(
            ValidationErrorResponse(
                message = "유효성 검사에 실패했습니다.",
                code = ErrorCodes.INVALID_ARGUMENT,
                errors = errors,
            ),
            HttpStatus.BAD_REQUEST,
        )
    }

    @ExceptionHandler(MissingRequestFieldException::class)
    fun handleMissingRequestFieldException(exception: MissingRequestFieldException): ResponseEntity<ApiResult<Nothing>> =
        ResponseEntity(
            ValidationErrorResponse(
                message = exception.message ?: "필수 요청 파라미터가 누락되었습니다.",
                code = ErrorCodes.MISSING_FIELD,
            ),
            HttpStatus.BAD_REQUEST,
        )
}

package com.tangerine.api.global.handler

import com.fasterxml.jackson.databind.exc.MismatchedInputException
import com.tangerine.api.global.response.ApiResult
import com.tangerine.api.global.response.ValidationError
import com.tangerine.api.global.response.ValidationErrorResponse
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handlerMethodArgumentNotValidException(exception: MethodArgumentNotValidException): ResponseEntity<ApiResult<Nothing>> {
        val errors =
            exception.bindingResult.fieldErrors.map { fieldError ->
                ValidationError(
                    field = fieldError.field,
                    message = fieldError.defaultMessage ?: "Invalid value",
                )
            }

        val response =
            ValidationErrorResponse(
                message = "유효성 검사에 실패했습니다.",
                code = "NOT_VALID_ARGUMENT",
                errors = errors,
            )

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }

    @ExceptionHandler(HttpMessageNotReadableException::class)
    fun handleJsonParseError(
        exception: HttpMessageNotReadableException,
        request: HttpServletRequest,
    ): ResponseEntity<ValidationErrorResponse> {
        val errors = mutableListOf<ValidationError>()

        val cause = exception.mostSpecificCause
        if (cause is MismatchedInputException) {
            cause.path?.forEach { ref ->
                errors.add(
                    ValidationError(
                        field = ref.fieldName ?: "(unknown)",
                        message = "${ref.from?.javaClass?.simpleName}.${ref.fieldName} 필드가 누락되었거나 형식이 잘못되었습니다.",
                    ),
                )
            }
        }

        val response =
            ValidationErrorResponse(
                message = "요청 본문이 잘못되었습니다.",
                code = "INVALID_JSON_REQUEST",
                errors = errors,
            )

        return ResponseEntity(response, HttpStatus.BAD_REQUEST)
    }
}

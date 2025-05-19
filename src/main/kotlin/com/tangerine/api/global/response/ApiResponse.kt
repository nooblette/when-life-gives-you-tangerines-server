package com.tangerine.api.global.response

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity

sealed class ApiResult<out T>

class Success<out T>(
    val data: T,
) : ApiResult<T>()

open class Error(
    val message: String,
    val code: String,
) : ApiResult<Nothing>()

class ValidationErrorResponse(
    message: String,
    code: String,
    val errors: List<ValidationError> = emptyList(),
) : Error(message, code)

class ValidationError(
    val field: String,
    val message: String,
)

fun <T> ApiResult<T>.toResponseEntity(): ResponseEntity<out ApiResult<T>> =
    when (this) {
        is Error -> ResponseEntity(this, HttpStatus.BAD_REQUEST)
        is Success -> ResponseEntity(this, HttpStatus.OK)
    }

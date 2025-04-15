package com.tangerine.api.global.response

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
    val errors: List<ValidationError>,
) : Error(message, code)

class ValidationError(
    val field: String,
    val message: String,
)

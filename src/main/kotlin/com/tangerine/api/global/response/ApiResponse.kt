package com.tangerine.api.global.response

sealed interface ApiResponse<out T> {
    class Success<out T>(
        val data: T,
    ) : ApiResponse<T>

    open class Error(
        val message: String,
        val code: String,
    ) : ApiResponse<Nothing>

    class ValidationErrorResponse(
        message: String,
        code: String,
        val errors: List<ValidationError> = emptyList(),
    ) : Error(message, code)

    class ValidationError(
        val field: String,
        val message: String,
    )
}

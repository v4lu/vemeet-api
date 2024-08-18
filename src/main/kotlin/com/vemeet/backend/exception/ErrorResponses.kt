package com.vemeet.backend.exception
data class ErrorResponse(
    val statusCode: Int,
    val message: String? = null,
    val errors: List<FieldError>? = null
)

data class FieldError(
    val path: String,
    val error: String
)
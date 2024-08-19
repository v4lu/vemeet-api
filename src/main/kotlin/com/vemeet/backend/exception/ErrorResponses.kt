package com.vemeet.backend.exception

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Error response")
data class ErrorResponse(
    @Schema(description = "HTTP status code", example = "400")
    val statusCode: Int,

    @Schema(description = "Error message", example = "Bad Request")
    val message: String? = null,

    @Schema(description = "List of field-specific errors")
    val errors: List<FieldError>? = null
)

@Schema(description = "Field-specific error")
data class FieldError(
    @Schema(description = "Field path", example = "email")
    val path: String,

    @Schema(description = "Error message", example = "must be a well-formed email address")
    val error: String
)
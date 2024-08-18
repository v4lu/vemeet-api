package com.vemeet.backend.dto

import jakarta.validation.constraints.*
import java.time.Instant
import java.time.LocalDate

data class SignUpRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
        message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    val password: String,

    @field:NotNull(message = "Birthday is required")
    @field:Past(message = "Birthday must be in the past")
    val birthday: LocalDate
)
data class AuthMessageResponse(val message: String)


data class ConfirmRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @field:NotBlank(message = "Confirmation Code is required")
    val confirmationCode: String
)

data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String,
    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
        message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    val password: String
)

data class LoginResponse(
    val cognitoId: String,
    val refreshToken: String,
    val refreshTokenExpiry: Instant,
    val accessToken: String,
    val accessTokenExpiry: Instant
)

data class RefreshTokenResponse(
    val accessToken: String,
    val accessTokenExpiry: Instant
)

package com.vemeet.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import java.time.Instant
import java.time.LocalDate

@Schema(description = "Sign up request")
data class SignUpRequest(
    @field:NotBlank(message = "Username is required")
    @field:Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    @Schema(description = "User's username", example = "johndoe")
    val username: String,

    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @Schema(description = "User's email address", example = "user@example.com")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
        message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    @Schema(description = "User's password", example = "P@ssw0rd123")
    val password: String,

    @field:NotNull(message = "Birthday is required")
    @field:Past(message = "Birthday must be in the past")
    @Schema(description = "User's birthday", example = "1990-01-01")
    val birthday: LocalDate
)

@Schema(description = "Authentication message response")
data class AuthMessageResponse(
    @Schema(description = "Response message", example = "User registered successfully")
    val message: String
)

@Schema(description = "Confirmation request")
data class ConfirmRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @Schema(description = "User's email address", example = "user@example.com")
    val email: String,

    @field:NotBlank(message = "Confirmation Code is required")
    @Schema(description = "Confirmation code sent to user's email", example = "123456")
    val confirmationCode: String
)

@Schema(description = "Login request")
data class LoginRequest(
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    @Schema(description = "User's email address", example = "user@example.com")
    val email: String,

    @field:NotBlank(message = "Password is required")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
        message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    @Schema(description = "User's password", example = "P@ssw0rd123")
    val password: String
)

@Schema(description = "Login response")
data class LoginResponse(
    @Schema(description = "User's Cognito ID", example = "12345678-1234-1234-1234-123456789012")
    val cognitoId: String,

    @Schema(description = "Refresh token")
    val refreshToken: String,

    @Schema(description = "Refresh token expiry time")
    val refreshTokenExpiry: Instant,

    @Schema(description = "Access token")
    val accessToken: String,

    @Schema(description = "Access token expiry time")
    val accessTokenExpiry: Instant
)

@Schema(description = "Refresh token response")
data class RefreshTokenResponse(
    @Schema(description = "New access token")
    val accessToken: String,

    @Schema(description = "New access token expiry time")
    val accessTokenExpiry: Instant
)

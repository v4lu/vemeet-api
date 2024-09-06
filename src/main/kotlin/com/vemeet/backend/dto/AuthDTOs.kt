package com.vemeet.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.*
import org.hibernate.validator.constraints.URL
import java.time.Instant

@Schema(description = "Sign up request")
data class RegisterRequest(
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
    val birthday: Instant,

    @field:URL(message = "URL is required")
    @Schema(description = "User's profile image URL", example = "https://example.com/profile.jpg")
    val imageUrl: String? = null
)

@Schema(description = "Response object for successful registration")
data class RegisterResponse(
    @Schema(description = "Registered user's email", example = "user@example.com")
    val email: String?,

    @Schema(description = "Registered user's username", example = "johndoe")
    val username: String,

    @Schema(description = "Registered user's ID", example = "1")
    val id: Long,

    @Schema(description = "Registration timestamp", example = "2024-08-27T10:30:00.000Z")
    val createdAt: String,

    @Schema(description = "User's profile image URL", example = "https://example.com/profile.jpg")
    val imageUrl: String? = null
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

@Schema(description = "Response object for successful login")
data class LoginResponse(
    @Schema(description = "JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,

    @Schema(description = "Access token expiry timestamp", example = "2024-08-27T11:30:00.000Z")
    val accessTokenExpiry: String,

    @Schema(description = "JWT refresh token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val refreshToken: String,

    @Schema(description = "Refresh token expiry timestamp", example = "2024-08-28T10:30:00.000Z")
    val refreshTokenExpiry: String,

    @Schema(description = "Aws cognito ID", example = "awsa12-3213")
    val cognitoId: String,
)

@Schema(description = "Response object for successful token refresh")
data class RefreshResponse(
    @Schema(description = "New JWT access token", example = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...")
    val accessToken: String,

    @Schema(description = "New access token expiry timestamp", example = "2024-08-27T12:30:00.000Z")
    val accessTokenExpiry: String,
)


@Schema(description = "Request to initiate password reset")
data class PasswordResetInitiateRequest(
    @Schema(description = "User's email address", example = "user@example.com", required = true)
    @field:NotBlank(message = "email is required")
    @field:Email(message = "Invalid email format")
    val email: String
)

@Schema(description = "Request to complete password reset")
data class PasswordResetCompleteRequest(
    @Schema(description = "User's email address", example = "user@example.com", required = true)
    @field:NotBlank(message = "New email is required")
    @field:Email(message = "Invalid email format")
    val email: String,

    @Schema(description = "New password", example = "newPassword123", required = true)
    @field:NotBlank(message = "Current Password is required")
    @field:Size(min = 8, message = "Current Password must be at least 8 characters long")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
        message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    val newPassword: String,

    @Schema(description = "Confirmation code received via email", example = "123456", required = true)
    @field:NotBlank(message = "Confirmation code is required")
    val confirmationCode: String
)

@Schema(description = "Request to initiate email change")
data class UpdateEmailRequest(
    @Schema(description = "New email address to change to", example = "newemail@example.com", required = true)
    @field:NotBlank(message = "New email is required")
    @field:Email(message = "Invalid email format")
    val newEmail: String
)

@Schema(description = "Request to resend verification email")
data class ResendVerificationEmailRequest(
    @Schema(description = "User's email address", example = "user@example.com", required = true)
    @field:NotBlank(message = "Email is required")
    @field:Email(message = "Invalid email format")
    val email: String
)

@Schema(description = "Request to change password")
data class ChangePasswordRequest(
    @Schema(description = "Current password", example = "oldPassword123", required = true)
    @field:NotBlank(message = "Current Password is required")
    @field:Size(min = 8, message = "Current Password must be at least 8 characters long")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
        message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    val oldPassword: String,

    @Schema(description = "New password", example = "newPassword123", required = true)
    @field:NotBlank(message = "New Password is required")
    @field:Size(min = 8, message = "New Password must be at least 8 characters long")
    @field:Pattern(
        regexp = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?]).*$",
        message = "Password must contain at least one digit, one lowercase, one uppercase, and one special character"
    )
    val newPassword: String
)

@Schema(description = "Request to confirm email change")
data class ConfirmEmailChangeRequest(
    @Schema(description = "Confirmation code sent to the new email address", example = "123456", required = true)
    @field:NotBlank(message = "Confirmation code is required")
    val confirmationCode: String
)

package com.vemeet.backend.controller

import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.ConflictException
import com.vemeet.backend.exception.EmailAlreadyExistsException
import com.vemeet.backend.security.CognitoService
import com.vemeet.backend.service.AuthService
import com.vemeet.backend.utils.extractAccessToken
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.apache.coyote.BadRequestException
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Authentication", description = "Authentication endpoints")
class AuthController(
    val authService: AuthService,
    val cognitoService: CognitoService,
) {

    private val logger = LoggerFactory.getLogger(AuthController::class.java)

    @PostMapping("/register")
    @Operation(
        summary = "Register a new user",
        responses = [
            ApiResponse(responseCode = "201", description = "Successfully registered user",
                content = [Content(schema = Schema(implementation = RegisterResponse::class))]),
            ApiResponse(responseCode = "409", description = "Bad request - email/username exist",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "422", description = "Invalid Request - some fields not passing validation",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))])
        ]
    )
    fun register(@Valid @RequestBody regReq: RegisterRequest): ResponseEntity<RegisterResponse> {
        try {
            if (authService.findByUsername(regReq.username) != null) {
                throw ConflictException("Username already exists")
            }

            val additionalAttributes = mapOf(
                "name" to regReq.username,
            )

            val signUpResult = cognitoService.signUp(regReq.email, regReq.password, additionalAttributes)
            val userRes = authService.createUser(regReq, signUpResult.userSub)

            return ResponseEntity.ok(userRes)
        } catch (e: EmailAlreadyExistsException) {
            // If the email already exists in Cognito, we don't need to do anything else
            throw EmailAlreadyExistsException("Email already exists")
        } catch (e: Exception) {
            try {
                cognitoService.deleteUser(regReq.email)
            } catch (deleteException: Exception) {
                logger.error("Failed to delete Cognito user after error: ${deleteException.message}")
            }
            throw e
        }
    }

    @PostMapping("/confirm")
    @Operation(
        summary = "Confirm user registration",
        description = "Confirm user registration with email and confirmation code",
        responses = [
            ApiResponse(responseCode = "200", description = "User confirmed successfully",
                content = [Content(schema = Schema(implementation = AuthMessageResponse::class))]),
            ApiResponse(responseCode = "400", description = "Bad request",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "422", description = "Validation error - invalid input",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))])
        ]
    )
    fun confirmUser(@Valid @RequestBody confirmRequest: ConfirmRequest): ResponseEntity<AuthMessageResponse> {
        cognitoService.confirmSignUp(confirmRequest.email, confirmRequest.confirmationCode)
        val awsId = cognitoService.getCognitoUserByEmail(confirmRequest.email)
        authService.markUserAsVerified(awsId)
        return ResponseEntity.ok(AuthMessageResponse("User confirmed successfully."))
    }

    @PostMapping("/login")
    @Operation(
        summary = "Login a new user",
        responses = [
            ApiResponse(responseCode = "200", description = "Successfully logged in",
                content = [Content(schema = Schema(implementation = LoginResponse::class))]),
            ApiResponse(responseCode = "401", description = "Invalid Credentials even if user not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "422", description = "Validation error - invalid input",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))])
        ]
    )
    fun login(@Valid @RequestBody logReq: LoginRequest): ResponseEntity<LoginResponse> {
        return authService.login(logReq)
    }


    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh access token",
        responses = [
            ApiResponse(responseCode = "200", description = "Successfully refreshed token",
                content = [Content(schema = Schema(implementation = RefreshResponse::class))]),
            ApiResponse(responseCode = "401", description = "Invalid refresh token",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "400", description = "Refresh token missing or aws cognito id",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))])
        ]
    )
    fun refreshAccessToken(
        @RequestHeader("Refresh-Token-X") refreshToken: String?,
        @RequestParam("id") awsId: String?
    ): ResponseEntity<RefreshResponse> {
        if (refreshToken.isNullOrBlank() || awsId.isNullOrBlank()) {
            throw BadRequestException("Refresh-Token and id are required")
        }

        val response = authService.refreshAccessToken(refreshToken, awsId)
        return ResponseEntity.ok(response)
    }

    @PostMapping("/password-reset/initiate")
    @Operation(
        summary = "Initiate password reset",
        description = "Sends a password reset code to the user's email",
        responses = [
            ApiResponse(responseCode = "200", description = "Password reset initiated successfully",
                content = [Content(schema = Schema(implementation = AuthMessageResponse::class))]),
            ApiResponse(responseCode = "400", description = "Invalid email",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "422", description = "Validation error - invalid input",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))])
        ]
    )
    fun initiatePasswordReset(@Valid @RequestBody request: PasswordResetInitiateRequest): ResponseEntity<AuthMessageResponse> {
        authService.initiatePasswordReset(request.email)
        return ResponseEntity.ok(AuthMessageResponse("Password reset initiated. Check your email for further instructions."))
    }

    @PostMapping("/password-reset/complete")
    @Operation(
        summary = "Complete password reset",
        description = "Resets the user's password using the provided reset code",
        responses = [
            ApiResponse(responseCode = "200", description = "Password reset completed successfully",
                content = [Content(schema = Schema(implementation = AuthMessageResponse::class))]),
            ApiResponse(responseCode = "400", description = "Invalid or expired reset code",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "422", description = "Validation error - invalid input",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))])
        ]
    )
    fun completePasswordReset(@Valid @RequestBody request: PasswordResetCompleteRequest): ResponseEntity<AuthMessageResponse> {
        authService.completePasswordReset(request.email, request.newPassword, request.confirmationCode)
        return ResponseEntity.ok(AuthMessageResponse("Password has been reset successfully."))
    }

    @PatchMapping("/email/initiate")
    @Operation(
        summary = "Initiate email address change",
        description = "Initiates the process of changing the email address for the authenticated user",
        responses = [
            ApiResponse(responseCode = "200", description = "Email change initiated successfully",
                content = [Content(schema = Schema(implementation = AuthMessageResponse::class))]),
            ApiResponse(responseCode = "400", description = "Invalid email",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "409", description = "Email already in use",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "422", description = "Validation error - invalid input",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))])
        ]
    )
    fun initiateEmailChange(
        @RequestHeader("Authorization") authHeader: String,
        @Valid @RequestBody request: UpdateEmailRequest
    ): ResponseEntity<AuthMessageResponse> {
        val accessToken = extractAccessToken(authHeader)
        authService.initiateEmailChange(accessToken, request.newEmail)
        return ResponseEntity.ok(AuthMessageResponse("Email change initiated. Check your new email for a confirmation code."))
    }

    @PostMapping("/email/confirm")
    @Operation(
        summary = "Confirm email address change",
        description = "Confirms the email address change using the provided confirmation code",
        responses = [
            ApiResponse(responseCode = "200", description = "Email changed successfully",
                content = [Content(schema = Schema(implementation = AuthMessageResponse::class))]),
            ApiResponse(responseCode = "400", description = "Invalid or expired confirmation code",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "422", description = "Validation error - invalid input",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))])
        ]
    )
    fun confirmEmailChange(
        @RequestHeader("Authorization") authHeader: String,
        @Valid @RequestBody request: ConfirmEmailChangeRequest
    ): ResponseEntity<AuthMessageResponse> {
        val accessToken = extractAccessToken(authHeader)
        authService.confirmEmailChange(accessToken, request.confirmationCode)
        return ResponseEntity.ok(AuthMessageResponse("Email changed successfully."))
    }

    @PostMapping("/verification-email/resend")
    @Operation(
        summary = "Resend verification email",
        description = "Resends the verification email to the user",
        responses = [
            ApiResponse(responseCode = "200", description = "Verification email sent successfully",
                content = [Content(schema = Schema(implementation = AuthMessageResponse::class))]),
            ApiResponse(responseCode = "400", description = "Invalid email",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "422", description = "Validation error - invalid input",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))])
        ]
    )
    fun resendVerificationEmail(@Valid @RequestBody request: ResendVerificationEmailRequest): ResponseEntity<AuthMessageResponse> {
        authService.resendVerificationEmail(request.email)
        return ResponseEntity.ok(AuthMessageResponse("Verification email has been resent."))
    }

    @PatchMapping("/password")
    @Operation(
        summary = "Change password",
        description = "Changes the password for the authenticated user",
        responses = [
            ApiResponse(responseCode = "200", description = "Password changed successfully",
                content = [Content(schema = Schema(implementation = AuthMessageResponse::class))]),
            ApiResponse(responseCode = "400", description = "Invalid old password",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "422", description = "Validation error - invalid input",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))])
        ]
    )
    fun changePassword(
        @RequestHeader("Authorization") authHeader: String,
        @Valid @RequestBody request: ChangePasswordRequest
    ): ResponseEntity<AuthMessageResponse> {
        val accessToken = extractAccessToken(authHeader)
        authService.changePassword(accessToken, request.oldPassword, request.newPassword)
        return ResponseEntity.ok(AuthMessageResponse("Password changed successfully."))
    }

    @DeleteMapping("/account")
    @Operation(
        summary = "Delete user account",
        description = "Permanently deletes the user's account from both the application and Cognito",
        responses = [
            ApiResponse(responseCode = "200", description = "Account deleted successfully",
                content = [Content(schema = Schema(implementation = AuthMessageResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]),
            ApiResponse(responseCode = "500", description = "Internal server error",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))])
        ]
    )
    fun deleteAccount(@RequestHeader("Authorization") authHeader: String): ResponseEntity<AuthMessageResponse> {
        val accessToken = extractAccessToken(authHeader)
        authService.deleteAccount(accessToken)
        return ResponseEntity.ok(AuthMessageResponse("Account deleted successfully"))
    }
}

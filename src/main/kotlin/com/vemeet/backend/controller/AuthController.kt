package com.vemeet.backend.controller

import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.BadRequestException
import com.vemeet.backend.exception.EmailAlreadyExistsException
import com.vemeet.backend.exception.ErrorResponse
import com.vemeet.backend.service.CognitoService
import com.vemeet.backend.service.AuthService
import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/v1/auth")
@Tag(name = "Auth", description = "Auth management endpoints")
class AuthController(
    private val cognitoService: CognitoService,
    private val authService: AuthService
) {

    @PostMapping("/register")
    @Transactional
    @Operation(
        summary = "Register a new user",
        description = "Register a new user with email, password, username, and birthday",
        responses = [
            ApiResponse(responseCode = "200", description = "User registered successfully",
                content = [Content(schema = Schema(implementation = AuthMessageResponse::class))]),
            ApiResponse(responseCode = "400", description = "Bad request",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            ApiResponse(responseCode = "409", description = "Email already exists",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))])
        ]
    )
    fun signUp(@Valid @RequestBody signUpRequest: SignUpRequest): ResponseEntity<AuthMessageResponse> {
        try {
            if (authService.findByUsername(signUpRequest.username) != null) {
                throw BadRequestException("Username already exists")
            }

            val additionalAttributes = mapOf(
                "name" to signUpRequest.username,
                "birthdate" to signUpRequest.birthday.toString()
            )

            val signUpResult = cognitoService.signUp(signUpRequest.email, signUpRequest.password, additionalAttributes)
            authService.createUser(signUpRequest.username, signUpRequest.birthday, signUpResult.userSub)

            return ResponseEntity.ok(AuthMessageResponse("User registered successfully. Please check your email for confirmation code."))
        } catch (e: EmailAlreadyExistsException) {
            // If the email already exists in Cognito, we don't need to do anything else
            throw EmailAlreadyExistsException("Email already exists")
        } catch (e: Exception) {
            try {
                cognitoService.deleteUser(signUpRequest.email)
            } catch (deleteException: Exception) {
                println("Failed to delete Cognito user after error: ${deleteException.message}")
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
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            ApiResponse(responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))])
        ]
    )
    fun confirmSignUp(@Valid @RequestBody confirmRequest: ConfirmRequest): ResponseEntity<AuthMessageResponse> {
        cognitoService.confirmSignUp(confirmRequest.email, confirmRequest.confirmationCode)
        val awsId = cognitoService.getCognitoUserByEmail(confirmRequest.email)
        authService.markUserAsVerified(awsId)
        return ResponseEntity.ok(AuthMessageResponse("User confirmed successfully."))
    }

    @PostMapping("/login")
    @Operation(
        summary = "User login",
        description = "Authenticate user with email and password",
        responses = [
            ApiResponse(responseCode = "200", description = "Login successful",
                content = [Content(schema = Schema(implementation = LoginResponse::class))]),
            ApiResponse(responseCode = "401", description = "Invalid credentials",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            ApiResponse(responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))])
        ]
    )
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        val loginResponse = authService.login(loginRequest.email, loginRequest.password)
        return ResponseEntity.ok(loginResponse)
    }


    @PostMapping("/refresh")
    @Operation(
        summary = "Refresh Access Token",
        description = "Refresh the access token using a refresh token",
        responses = [
            ApiResponse(responseCode = "200", description = "Token refreshed successfully",
                content = [Content(schema = Schema(implementation = RefreshTokenResponse::class))]),
            ApiResponse(responseCode = "400", description = "Bad request",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            ApiResponse(responseCode = "401", description = "Invalid refresh token",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))])
        ]
    )
    fun refreshAccessToken(
        @RequestHeader("Refresh-Token") refreshToken: String?,
        @RequestParam("id") awsId: String?
    ): ResponseEntity<RefreshTokenResponse> {
        if (refreshToken.isNullOrBlank() || awsId.isNullOrBlank()) {
            throw BadRequestException("Refresh-Token and id are required")
        }

        val response = authService.refreshAccessToken(refreshToken, awsId)
        return ResponseEntity.ok(response)
    }

}

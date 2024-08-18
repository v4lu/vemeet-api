package com.vemeet.backend.controller

import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.BadRequestException
import com.vemeet.backend.exception.EmailAlreadyExistsException
import com.vemeet.backend.service.CognitoService
import com.vemeet.backend.service.AuthService
import jakarta.transaction.Transactional
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import jakarta.validation.Valid

@RestController
@RequestMapping("/v1/auth")
class AuthController(
    private val cognitoService: CognitoService,
    private val authService: AuthService
) {

    @PostMapping("/register")
    @Transactional
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
    fun confirmSignUp(@Valid @RequestBody confirmRequest: ConfirmRequest): ResponseEntity<AuthMessageResponse> {
        cognitoService.confirmSignUp(confirmRequest.email, confirmRequest.confirmationCode)
        val awsId = cognitoService.getCognitoUserByEmail(confirmRequest.email)
        authService.markUserAsVerified(awsId)
        return ResponseEntity.ok(AuthMessageResponse("User confirmed successfully."))
    }

    @PostMapping("/login")
    fun login(@Valid @RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        val loginResponse = authService.login(loginRequest.email, loginRequest.password)
        return ResponseEntity.ok(loginResponse)
    }


    @PostMapping("/refresh")
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

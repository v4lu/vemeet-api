package com.vemeet.backend.controller

import com.vemeet.backend.service.CognitoService
import com.vemeet.backend.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.time.LocalDate

@RestController
@RequestMapping("/api/auth")
class AuthController(
    private val cognitoService: CognitoService,
    private val userService: UserService
) {

    @PostMapping("/register")
    fun signUp(@RequestBody signUpRequest: SignUpRequest): ResponseEntity<String> {
        val additionalAttributes = mapOf(
            "name" to signUpRequest.username,
            "birthdate" to signUpRequest.birthday.toString()
        )
        val signUpResult = cognitoService.signUp(signUpRequest.email, signUpRequest.password, additionalAttributes)
        userService.createUser(signUpRequest.email, signUpRequest.birthday, signUpResult.userSub)
        return ResponseEntity.ok("User registered successfully. Please check your email for confirmation code.")
    }

    @PostMapping("/confirm")
    fun confirmSignUp(@RequestBody confirmRequest: ConfirmRequest): ResponseEntity<String> {
        cognitoService.confirmSignUp(confirmRequest.email, confirmRequest.confirmationCode)
        return ResponseEntity.ok("User confirmed successfully.")
    }


}

data class SignUpRequest(val username: String, val email: String, val password: String, val birthday: LocalDate)
data class ConfirmRequest(val email: String, val confirmationCode: String)
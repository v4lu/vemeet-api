package com.vemeet.backend.controller

import com.vemeet.backend.dto.ExceptionResponse
import com.vemeet.backend.dto.UserResponse
import com.vemeet.backend.dto.UserUpdateRequest
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.CognitoIdExtractor
import com.vemeet.backend.utils.extractAccessToken
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/users")
@Tag(name = "User", description = "User endpoints")
class UserController(
    private val userService: UserService,
) {

    @GetMapping
    @Operation(
        summary = "Get user session",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "User not found exception in db or ctx",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "401", description = "Invalid Credentials - something wrong with jwt",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun getSession(authentication: Authentication): ResponseEntity<UserResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        println(cognitoId)

        return ResponseEntity.ok(UserResponse.fromUser(user))
    }


    @PatchMapping
    @Operation(
        summary = "Update user profile",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully updated user profile",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "401", description = "Invalid Credentials",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "409", description = "Username taken",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
    )
        ]
    )
    fun updateUser(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody userUpdateRequest: UserUpdateRequest
    ): ResponseEntity<UserResponse> {
        val accessToken = extractAccessToken(authHeader)
        val updatedUser = userService.updateUser(accessToken, userUpdateRequest)
        return ResponseEntity.ok(UserResponse.fromUser(updatedUser))
    }


    @GetMapping("/{userId}")
    @Operation(
        summary = "Get user by id",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully updated user profile",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = UserResponse::class))]
            ),
        ]
    )
    fun getUser(
        @PathVariable("userId") userId: Long
    ): ResponseEntity<UserResponse> {
        return ResponseEntity.ok(userService.getUserById(userId))

    }
}

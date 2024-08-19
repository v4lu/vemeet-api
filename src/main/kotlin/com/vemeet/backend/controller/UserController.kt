package com.vemeet.backend.controller

import com.vemeet.backend.dto.SessionResponse
import com.vemeet.backend.exception.ErrorResponse
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.extractAccessToken
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag

@RestController
@RequestMapping("/v1/users")
@Tag(name = "User", description = "User management endpoints")
class UserController(private val userService: UserService) {

    @GetMapping("/")
    @Operation(
        summary = "Get current user",
        description = "Retrieves the details of the currently authenticated user",
        responses = [
            ApiResponse(responseCode = "200", description = "Successfully retrieved user details",
                content = [Content(schema = Schema(implementation = SessionResponse::class))]),
            ApiResponse(responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))]),
            ApiResponse(responseCode = "401", description = "Unauthorized - Authentication required",
                content = [Content(schema = Schema(implementation = ErrorResponse::class))])
        ]
    )
    fun getCurrentUserSession(@RequestHeader("Authorization") authHeader: String): ResponseEntity<SessionResponse> {
        val accessToken = extractAccessToken(authHeader)
        val userSession = userService.getUserSession(accessToken)
        return ResponseEntity.ok(userSession)
    }


}
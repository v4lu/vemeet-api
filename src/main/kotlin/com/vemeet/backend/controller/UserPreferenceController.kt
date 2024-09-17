package com.vemeet.backend.controller


import com.vemeet.backend.dto.ExceptionResponse
import com.vemeet.backend.dto.UserPreferenceRequest
import com.vemeet.backend.dto.UserPreferenceResponse
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.UserPreferenceService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.CognitoIdExtractor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/user-preferences")
@Tag(name = "User Preferences", description = "User Preferences endpoints")
class UserPreferenceController(
    private val userPreferenceService: UserPreferenceService,
    private val userService: UserService
) {

    @GetMapping
    @Operation(
        summary = "Get user preferences",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved user preferences",
                content = [Content(schema = Schema(implementation = UserPreferenceResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "User preferences not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun getUserPreferences(authentication: Authentication): ResponseEntity<UserPreferenceResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication) ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        return ResponseEntity.ok(userPreferenceService.getUserPreference(user))
    }

    @PostMapping
    @Operation(
        summary = "Create user preferences",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully created user preferences",
                content = [Content(schema = Schema(implementation = UserPreferenceResponse::class))]
            )
        ]
    )
    fun createUserPreferences(
        authentication: Authentication,
        @RequestBody request: UserPreferenceRequest
    ): ResponseEntity<UserPreferenceResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication) ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        return ResponseEntity.ok(userPreferenceService.createUserPreference(user, request))
    }

    @PatchMapping
    @Operation(
        summary = "Update user preferences",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully updated user preferences",
                content = [Content(schema = Schema(implementation = UserPreferenceResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "User preferences not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun updateUserPreferences(
        authentication: Authentication,
        @RequestBody request: UserPreferenceRequest
    ): ResponseEntity<UserPreferenceResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication) ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        return ResponseEntity.ok(userPreferenceService.updateUserPreference(user, request))
    }

    @DeleteMapping
    @Operation(
        summary = "Delete user preferences",
        responses = [
            ApiResponse(
                responseCode = "204",
                description = "Successfully deleted user preferences"
            ),
            ApiResponse(
                responseCode = "404",
                description = "User preferences not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun deleteUserPreferences(authentication: Authentication): ResponseEntity<Unit> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication) ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        userPreferenceService.deleteUserPreference(user)
        return ResponseEntity.noContent().build()
    }
}
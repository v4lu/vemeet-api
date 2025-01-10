package com.vemeet.backend.controller

import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.ChatService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.CognitoIdExtractor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Page
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/chats")
@Tag(name = "Chats", description = "Chat endpoints")
class ChatController(
    private val chatService: ChatService,
    private val userService: UserService
) {

    @GetMapping
    @Operation(
        summary = "Get all chats for the current user",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Retrieved chats successfully",
                content = [Content(schema = Schema(implementation = List::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    suspend fun getAllChats(authentication: Authentication): ResponseEntity<List<ChatResponse>> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val chats = chatService.getUserChats(user)
        return ResponseEntity.ok(chats)
    }

    @GetMapping("/{chatId}")
    @Operation(
        summary = "Get messages for a specific chat",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Retrieved messages successfully",
                content = [Content(schema = Schema(implementation = Page::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Chat not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Not allowed to access this chat",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    suspend fun getChatMessages(
        authentication: Authentication,
        @PathVariable chatId: Long,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "15") size: Int
    ): ResponseEntity<Page<MessageResponse>> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication) ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val messages = chatService.getChatMessages(chatId, user, page, size)
        return ResponseEntity.ok(messages)
    }

    @PostMapping("/message")
    @Operation(
        summary = "Send a new message in a specific chat",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Message sent successfully",
                content = [Content(schema = Schema(implementation = MessageResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Chat not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Not allowed to send message in this chat",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    suspend fun sendMessage(
        authentication: Authentication,
        @Valid @RequestBody request: SendMessageRequest
    ): ResponseEntity<MessageResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val message = chatService.sendMessage(user, request)
        return ResponseEntity.ok(message)
    }


    @GetMapping("/users/{receiverId}")
    @Operation(
        summary = "Get all chats for the current user",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Retrieved chats successfully",
                content = [Content(schema = Schema(implementation = ChatResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
     fun getChatByUsers(
        authentication: Authentication,
        @PathVariable receiverId: Long
     ): ResponseEntity<ChatResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val chats = chatService.getChatByUsers(user, receiverId)
        return ResponseEntity.ok(chats)
    }


    @GetMapping("/{chatId}/assets")
    @Operation(
        summary = "Get assets for a specific chat",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Retrieved assets successfully",
                content = [Content(schema = Schema(implementation = Page::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Chat not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Not allowed to access this chat",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    suspend fun getChatAssets(
        authentication: Authentication,
        @PathVariable chatId: Long,
        @RequestParam assetTypes: List<String>,
        @RequestParam(defaultValue = "0") page: Int,
        @RequestParam(defaultValue = "15") size: Int
    ): ResponseEntity<Page<ChatAssetResponse>> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication) ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val assets = chatService.getChatAssets(chatId, user, assetTypes, page, size)
        return ResponseEntity.ok(assets)
    }

}


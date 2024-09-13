package com.vemeet.backend.controller

import com.vemeet.backend.dto.ChatResponse
import com.vemeet.backend.dto.ExceptionResponse
import com.vemeet.backend.dto.MessageDTO
import com.vemeet.backend.dto.SendMessageRequest
import com.vemeet.backend.service.ChatService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.extractAccessToken
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestHeader
import org.springframework.web.bind.annotation.RequestMapping
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
    fun getAllChats(@RequestHeader("Authorization") authHeader: String): ResponseEntity<List<ChatResponse>> {
        val accessToken = extractAccessToken(authHeader)
        val user = userService.getSessionUser(accessToken)
        val chats = chatService.getUserChats(user.id)
        return ResponseEntity.ok(chats)
    }

    @GetMapping("/{chatId}")
    @Operation(
        summary = "Get messages for a specific chat",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Retrieved messages successfully",
                content = [Content(schema = Schema(implementation = List::class))]
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
    fun getChatMessages(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable chatId: Long
    ): ResponseEntity<List<MessageDTO>> {
        val accessToken = extractAccessToken(authHeader)
        val user = userService.getSessionUser(accessToken)
        val messages = chatService.getChatMessages(chatId, user)
        return ResponseEntity.ok(messages)
    }

    @PostMapping("/create")
    @Operation(
        summary = "Create a new chat between two users",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Chat created successfully",
                content = [Content(schema = Schema(implementation = ChatResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "User not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun createChat(
        @RequestHeader("Authorization") authHeader: String,
        @RequestBody request: CreateChatRequest
    ): ResponseEntity<ChatResponse> {
        val accessToken = extractAccessToken(authHeader)
        val user = userService.getSessionUser(accessToken)
        val chat = chatService.createChat(user.id, request.otherUserId)
        return ResponseEntity.ok(chat)
    }

    @PostMapping("/{chatId}/messages")
    @Operation(
        summary = "Send a new message in a specific chat",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Message sent successfully",
                content = [Content(schema = Schema(implementation = MessageDTO::class))]
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
    fun sendMessage(
        @RequestHeader("Authorization") authHeader: String,
        @PathVariable chatId: Long,
        @RequestBody request: SendMessageRequest
    ): ResponseEntity<MessageDTO> {
        val accessToken = extractAccessToken(authHeader)
        val user = userService.getSessionUser(accessToken)
        val message = chatService.sendMessage(user, chatId, request)
        return ResponseEntity.ok(message)
    }
}

data class CreateChatRequest(
    val otherUserId: Long
)
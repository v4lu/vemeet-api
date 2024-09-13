package com.vemeet.backend.dto

import com.vemeet.backend.model.User
import java.time.Instant

data class ChatDTO(
    val id: Long,
    val user1: User,
    val user2: User,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class MessageDTO(
    val id: Long,
    val chatId: Long,
    val senderId: Long,
    val messageType: String,
    val content: String, // Decrypted content
    val createdAt: Instant,
    val readAt: Instant?,
    val isOneTime: Boolean
)

data class SendMessageRequest(
    val recipientId: Long,
    val messageType: String,
    val content: String,
    val isOneTime: Boolean = false
)

// ChatAssetDTO remains the same
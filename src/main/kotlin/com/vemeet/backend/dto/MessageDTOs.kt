package com.vemeet.backend.dto

import com.vemeet.backend.model.Chat
import com.vemeet.backend.model.Message
import com.vemeet.backend.model.User
import java.time.Instant

data class ChatDTO(
    val id: Long,
    val user1: User,
    val user2: User,
    val createdAt: Instant,
    val updatedAt: Instant
) {
    companion object {
        fun from(chat: Chat): ChatDTO {
            return ChatDTO(
                id = chat.id,
                user1 = chat.user1,
                user2 = chat.user2,
                createdAt = chat.createdAt,
                updatedAt = chat.updatedAt
            )
        }
    }
}

data class MessageDTO(
    val id: Long,
    val chatId: Long,
    val senderId: Long,
    val messageType: String,
    val content: String,
    val createdAt: Instant,
    val readAt: Instant?,
    val isOneTime: Boolean
) {
    companion object {
        fun from(message: Message, decryptedContent: String): MessageDTO {
            return MessageDTO(
                id = message.id,
                chatId = message.chat.id,
                senderId = message.sender.id,
                messageType = message.messageType,
                content = decryptedContent,
                createdAt = message.createdAt,
                readAt = message.readAt,
                isOneTime = message.isOneTime
            )
        }
    }
}

data class SendMessageRequest(
    val recipientId: Long,
    val messageType: String,
    val content: String,
    val isOneTime: Boolean = false
)

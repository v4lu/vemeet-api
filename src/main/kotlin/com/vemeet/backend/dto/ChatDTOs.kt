package com.vemeet.backend.dto

import com.vemeet.backend.model.Chat
import com.vemeet.backend.model.Message
import com.vemeet.backend.model.User
import java.time.format.DateTimeFormatter

data class ChatResponse(
    val id: Long,
    val user1: User,
    val user2: User,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(chat: Chat): ChatResponse {
            return ChatResponse(
                id = chat.id,
                user1 = chat.user1,
                user2 = chat.user2,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(chat.createdAt),
                updatedAt = DateTimeFormatter.ISO_INSTANT.format(chat.updatedAt)
            )
        }
    }
}

data class MessageDTO(
    val id: Long,
    val chatId: Long,
    val sender: User,
    val messageType: String,
    val content: String,
    val createdAt: String,
    val readAt: String?,
    val isOneTime: Boolean
) {
    companion object {
        fun from(message: Message, decryptedContent: String): MessageDTO {
            return MessageDTO(
                id = message.id,
                chatId = message.chat.id,
                sender = message.sender,
                messageType = message.messageType,
                content = decryptedContent,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(message.createdAt),
                readAt = message.readAt?.let { DateTimeFormatter.ISO_INSTANT.format(it) },
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

data class CreateChatRequest(
    val otherUserId: Long
)
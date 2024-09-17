package com.vemeet.backend.dto

import com.vemeet.backend.model.Chat
import com.vemeet.backend.model.Message
import com.vemeet.backend.model.User
import java.time.format.DateTimeFormatter

data class ChatResponse(
    val id: Long,
    val sessionUser: User,
    val otherUser: User,
    val lastMessage: MessageResponse?,
    val sessionUserSeenStatus: Boolean,
    val otherUserSeenStatus: Boolean,
    val createdAt: String,
    val updatedAt: String
) {
    companion object {
        fun from(chat: Chat, sessionUser: User, lastMessage: MessageResponse?): ChatResponse {
            val otherUser = if (chat.user1.id == sessionUser.id) chat.user2 else chat.user1
            val (sessionUserSeenStatus, otherUserSeenStatus) = if (sessionUser.id == chat.user1.id) {
                chat.user1SeenStatus to chat.user2SeenStatus
            } else {
                chat.user2SeenStatus to chat.user1SeenStatus
            }

            return ChatResponse(
                id = chat.id,
                sessionUser = sessionUser,
                otherUser = otherUser,
                lastMessage = lastMessage,
                sessionUserSeenStatus = sessionUserSeenStatus,
                otherUserSeenStatus = otherUserSeenStatus,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(chat.createdAt),
                updatedAt = DateTimeFormatter.ISO_INSTANT.format(chat.updatedAt)
            )
        }
    }
}

data class MessageResponse(
    val id: Long,
    val chatId: Long,
    val sender: User,
    val messageType: String,
    val content: String?,
    val createdAt: String,
    val readAt: String?,
    val isOneTime: Boolean,
    val recipient: User,
    val isSessionUserSender: Boolean
) {
    companion object {
        fun from(message: Message, decryptedContent: String?, sessionUser: User): MessageResponse {
            val recipient = if (message.sender.id == message.chat.user1.id) message.chat.user2 else message.chat.user1
            val isSessionUserSender = message.sender.id == sessionUser.id

            return MessageResponse(
                id = message.id,
                chatId = message.chat.id,
                sender = message.sender,
                messageType = message.messageType,
                content = decryptedContent,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(message.createdAt),
                readAt = message.readAt?.let { DateTimeFormatter.ISO_INSTANT.format(it) },
                isOneTime = message.isOneTime,
                recipient = recipient,
                isSessionUserSender = isSessionUserSender
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

data class ChatWithLastMessage(
    val chat: Chat,
    val lastMessage: Message?
)
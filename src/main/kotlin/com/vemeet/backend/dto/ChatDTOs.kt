package com.vemeet.backend.dto

import com.fasterxml.jackson.annotation.JsonProperty
import com.vemeet.backend.model.*
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


data class SendMessageRequest(
    val recipientId: Long,
    val messageType: MessageType,
    val content: String,
    val isOneTime: Boolean = false,
    val firstTime: Boolean,
    val chatAssets: List<ChatAssetRequest>?
)


data class MessageResponse(
    val id: Long,
    val chatId: Long,
    val sender: User,
    val messageType: MessageType,
    val content: String?,
    val contentPreview: String?,
    val createdAt: String,
    val readAt: String?,
    val isOneTime: Boolean,
    val recipient: User,
    val isSessionUserSender: Boolean,
    val chatAssets: List<ChatAssetResponse>?
) {
    companion object {
        fun from(message: Message, decryptedContent: String?, sessionUser: User, chatAssets: List<ChatAsset>?, decryptedFileUrls: List<String?>?): MessageResponse {
            val recipient = if (message.sender.id == message.chat.user1.id) message.chat.user2 else message.chat.user1
            val isSessionUserSender = message.sender.id == sessionUser.id

            return MessageResponse(
                id = message.id,
                chatId = message.chat.id,
                sender = message.sender,
                messageType = message.messageType,
                content = decryptedContent,
                contentPreview = message.contentPreview,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(message.createdAt),
                readAt = message.readAt?.let { DateTimeFormatter.ISO_INSTANT.format(it) },
                isOneTime = message.isOneTime,
                recipient = recipient,
                isSessionUserSender = isSessionUserSender,
                chatAssets = chatAssets?.mapIndexed { index, asset ->
                    ChatAssetResponse.from(asset, decryptedFileUrls?.getOrNull(index))
                }
            )
        }
    }
}

data class ChatAssetResponse(
    val id: Long,
    val messageId: Long,
    val chatId: Long,
    val fileType: String,
    val fileSize: Long,
    val mimeType: String?,
    val durationSeconds: Int?,
    val fileUrl: String?,
    val createdAt: String
) {
    companion object {
        fun from(chatAsset: ChatAsset, decryptedFileUrl: String?): ChatAssetResponse {
            return ChatAssetResponse(
                id = chatAsset.id,
                messageId = chatAsset.message.id,
                chatId = chatAsset.chat.id,
                fileType = chatAsset.fileType,
                fileSize = chatAsset.fileSize,
                mimeType = chatAsset.mimeType,
                durationSeconds = chatAsset.durationSeconds,
                fileUrl = decryptedFileUrl,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(chatAsset.createdAt)
            )
        }
    }
}



data class ChatWithLastMessage(
    val chat: Chat,
    val lastMessage: Message?
)


data class EncryptionResponse(
    @JsonProperty("encrypted_message")
    val encryptedMessage: String,

    @JsonProperty("encrypted_data_key")
    val encryptedDataKey: String,

    @JsonProperty("key_version")
    val keyVersion: Int
)

data class ChatAssetRequest(
    val fileType: String,
    val fileSize: Long,
    val mimeType: String?,
    val durationSeconds: Int?,
    val assetUrl: String
)
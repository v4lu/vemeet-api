package com.vemeet.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vemeet.backend.dto.ChatResponse
import com.vemeet.backend.dto.MessageDTO
import com.vemeet.backend.dto.SendMessageRequest
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.Chat
import com.vemeet.backend.model.Message
import com.vemeet.backend.model.User
import com.vemeet.backend.repository.ChatRepository
import com.vemeet.backend.repository.MessageRepository
import com.vemeet.backend.repository.UserRepository
import com.vemeet.backend.security.EncryptedData
import com.vemeet.backend.security.EncryptionService
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.nio.ByteBuffer
import java.time.Instant


@Service
class ChatService(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val encryptionService: EncryptionService,
    private val chatRepository: ChatRepository,
    private val objectMapper: ObjectMapper,
    private val chatWebSocketService: ChatWebSocketService,
) {
    @Transactional
    fun sendMessage(sender: User, chatId: Long, request: SendMessageRequest): MessageDTO {
        val chat = chatRepository.findById(chatId).orElseThrow { ResourceNotFoundException("Chat not found") }

        if (chat.user1.id != sender.id && chat.user2.id != sender.id) {
            throw NotAllowedException("You don't have access to this chat")
        }

        val recipient = if (chat.user1.id == sender.id) chat.user2 else chat.user1

        if (recipient.inboxLocked && !isFollowing(sender.id, recipient.id)) {
            throw NotAllowedException("Cannot send message to this user")
        }

        val encryptedData = encryptionService.encrypt(request.content)
        val message = Message(
            chat = chat,
            sender = sender,
            messageType = request.messageType,
            encryptedContent = encryptedData.encryptedContent,
            encryptionType = encryptedData.encryptionType,
            encryptedDataKey = encryptedData.encryptedDataKey.array(),
            encryptionIv = encryptedData.iv,
            isOneTime = request.isOneTime
        )

        val savedMessage = messageRepository.save(message)
        chat.updatedAt = Instant.now()
        chatRepository.save(chat)
        val messageDTO = decryptedMessage(savedMessage)

        val webSocketMessage = objectMapper.writeValueAsString(messageDTO)
        chatWebSocketService.sendMessage(recipient.id, webSocketMessage)

        return messageDTO
    }
    fun getUserChats(userId: Long): List<ChatResponse> {
        val chats = chatRepository.findByUser1IdOrUser2Id(userId, userId)
        return chats.map { ChatResponse.from(it) }
    }

    fun getChatMessages(chatId: Long, user: User, page: Int, size: Int): Page<MessageDTO> {
        val chat = chatRepository.findById(chatId).orElseThrow { ResourceNotFoundException("Chat not found") }
        if (chat.user1.id != user.id && chat.user2.id != user.id) {
            throw NotAllowedException("You don't have access to this chat")
        }
        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val messages = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable)
        return messages.map { decryptedMessage(it) }
    }


    private fun decryptedMessage(message: Message): MessageDTO {
        val decryptedContent = encryptionService.decrypt(
            EncryptedData(
                encryptedContent = message.encryptedContent ?: ByteArray(0),
                encryptionType = message.encryptionType,
                encryptedDataKey = ByteBuffer.wrap(message.encryptedDataKey ?: ByteArray(0)),
                iv = message.encryptionIv ?: ByteArray(0)
            )
        )

        return MessageDTO.from(message, decryptedContent)
    }

    @Transactional
    fun createChat(userId: Long, otherUserId: Long): ChatResponse {
        val user = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("User not found") }
        val otherUser = userRepository.findById(otherUserId).orElseThrow { ResourceNotFoundException("Other user not found") }

        val existingChat = chatRepository.findChatBetweenUsers(user, otherUser)
        if (existingChat != null) {
            return ChatResponse.from(existingChat)

        }

        val newChat = Chat(user1 = user, user2 = otherUser)
        val savedChat = chatRepository.save(newChat)
        return ChatResponse.from(savedChat)

    }


    private fun isFollowing(followerId: Long, followedId: Long): Boolean {
        return true // Placeholder
    }
}

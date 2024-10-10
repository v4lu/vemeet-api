package com.vemeet.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vemeet.backend.dto.ChatResponse
import com.vemeet.backend.dto.EncryptionResponse
import com.vemeet.backend.dto.MessageResponse
import com.vemeet.backend.dto.SendMessageRequest
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.*
import com.vemeet.backend.repository.ChatRepository
import com.vemeet.backend.repository.MessageRepository
import com.vemeet.backend.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.time.Instant
import java.util.*

@Service
class ChatService(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val webClient: WebClient,
    private val objectMapper: ObjectMapper,
    private val userService: UserService,
    private val chatWebSocketService: ChatWebSocketService,
    private val notificationService: NotificationService,
) {
    @Transactional
    suspend fun sendMessage(sender: User, chatId: Long, request: SendMessageRequest): MessageResponse {
        val chat = withContext(Dispatchers.IO) {
            chatRepository.findById(chatId)
        }.orElseThrow { ResourceNotFoundException("Chat not found") }

        if (chat.user1.id != sender.id && chat.user2.id != sender.id) {
            throw NotAllowedException("You don't have access to this chat")
        }

        val recipient = if (chat.user1.id == sender.id) chat.user2 else chat.user1

        if (recipient.inboxLocked && !isFollowing(sender.id, recipient.id)) {
            throw NotAllowedException("Cannot send message to this user")
        }

        // Call external encryption service
        val encryptionResponse = try {
            webClient.post()
                .uri("http://encrpytion:9002/v1/crypto/encrypt")
                .bodyValue(mapOf("message" to request.content))
                .retrieve()
                .awaitBody<EncryptionResponse>()
        } catch (e: Exception) {
            throw RuntimeException("Failed to encrypt message: ${e.message}")
        }

        val message = Message(
            chat = chat,
            sender = sender,
            messageType = request.messageType,
            encryptedContent = if (request.content.isNotBlank()) Base64.getDecoder().decode(encryptionResponse.encryptedMessage) else null,
            encryptedDataKey = Base64.getDecoder().decode(encryptionResponse.encryptedDataKey),
            encryptionVersion = encryptionResponse.keyVersion,
            isOneTime = request.isOneTime
        )

        val savedMessage = withContext(Dispatchers.IO) {
            messageRepository.save(message)
        }

        chat.updatedAt = Instant.now()
        chat.lastMessage = savedMessage
        if (sender.id == chat.user1.id) {
            chat.user1SeenStatus = true
            chat.user2SeenStatus = false
        } else {
            chat.user1SeenStatus = false
            chat.user2SeenStatus = true
        }
        withContext(Dispatchers.IO) {
            chatRepository.save(chat)
        }

        val content = "New message from ${sender.username}"
        notificationService.createNotification(recipient.id, NotificationTypeEnum.NEW_MESSAGE.toString().lowercase(), content)
        val res = decryptedMessage(savedMessage, sender)
        chatWebSocketService.sendMessage(recipient.id, res)
        return res
    }

    suspend fun getUserChats(user: User): List<ChatResponse> {
        val chatsWithLastMessages = withContext(Dispatchers.IO) {
            chatRepository.findChatsWithLastMessageByUserId(user.id)
        }
        return chatsWithLastMessages.map { chatWithLastMessage ->
            ChatResponse.from(
                chat = chatWithLastMessage.chat,
                sessionUser = user,
                lastMessage = chatWithLastMessage.lastMessage?.let { decryptedMessage(it, user) }
            )
        }
    }

    fun getChatMessages(chatId: Long, user: User, page: Int, size: Int): Page<MessageResponse> {
        val chat = chatRepository.findById(chatId).orElseThrow { ResourceNotFoundException("Chat not found") }
        if (chat.user1.id != user.id && chat.user2.id != user.id) {
            throw NotAllowedException("You don't have access to this chat")
        }
        updateSeenStatus(chat, user)

        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val messagesPage = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable)

        // Use runBlocking to bridge between non-suspend and suspend functions
        val decryptedMessages = runBlocking(Dispatchers.Default) {
            messagesPage.content.map { message ->
                async { decryptedMessage(message, user) }
            }.map { it.await() }
        }

        // Create a new Page with decrypted messages
        return PageImpl(decryptedMessages, pageable, messagesPage.totalElements)
    }

    private suspend fun decryptedMessage(message: Message, sessionUser: User): MessageResponse {
        val decryptedContent = if (message.encryptedContent != null) {
            try {
                val decryptionResponse = webClient.post()
                    .uri("http://encrpytion:9002/v1/crypto/decrypt")
                    .bodyValue(mapOf(
                        "encrypted_message" to Base64.getEncoder().encodeToString(message.encryptedContent),
                        "encrypted_data_key" to Base64.getEncoder().encodeToString(message.encryptedDataKey)
                    ))
                    .retrieve()
                    .awaitBody<Map<String, String>>()
                decryptionResponse["decrypted_message"]
            } catch (e: Exception) {
                throw RuntimeException("Failed to decrypt message: ${e.message}")
            }
        } else {
            null
        }

        return MessageResponse.from(message, decryptedContent, sessionUser)
    }

    fun getChatByUsers(sessionUser: User, receiverId: Long) : ChatResponse {
        val receiver = userService.getUserByIdFull(receiverId)
        val chat = chatRepository.findChatBetweenUsers(sessionUser, receiver)
            ?: throw ResourceNotFoundException("Chat not found")

        return ChatResponse.from(chat, sessionUser, null)

    }

    @Transactional
    suspend fun createChat(userId: Long, otherUserId: Long): ChatResponse {
        val user = withContext(Dispatchers.IO) {
            userRepository.findById(userId)
        }.orElseThrow { ResourceNotFoundException("User not found") }

        val otherUser = withContext(Dispatchers.IO) {
            userRepository.findById(otherUserId)
        }.orElseThrow { ResourceNotFoundException("Other user not found") }

        val existingChat = withContext(Dispatchers.IO) {
            chatRepository.findChatBetweenUsers(user, otherUser)
        }
        if (existingChat != null) {
            val lastMessage = existingChat.lastMessage?.let { decryptedMessage(it, user) }
            return ChatResponse.from(existingChat, user, lastMessage)
        }

        val newChat = Chat(
            user1 = user,
            user2 = otherUser,
            user1SeenStatus = true,
            user2SeenStatus = false
        )

        val savedChat = withContext(Dispatchers.IO) {
            chatRepository.save(newChat)
        }

        return ChatResponse.from(savedChat, user, lastMessage = null)
    }

    private fun isFollowing(followerId: Long, followedId: Long): Boolean {
        return true // Placeholder
    }

    private  fun updateSeenStatus(chat: Chat, user: User) {
        if (user.id == chat.user1.id && !chat.user1SeenStatus) {
            chat.user1SeenStatus = true
            chatRepository.save(chat)
        } else if (user.id == chat.user2.id && !chat.user2SeenStatus) {
            chat.user2SeenStatus = true
            chatRepository.save(chat)
        }
    }
}

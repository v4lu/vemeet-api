package com.vemeet.backend.service

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
import org.apache.coyote.BadRequestException
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
    private val userService: UserService,
    private val chatWebSocketService: ChatWebSocketService,
    private val notificationService: NotificationService,
) {
    @Transactional
    suspend fun sendMessage(sender: User, request: SendMessageRequest): MessageResponse {
        val (chat, recipient) = if (request.firstTime) {
            createNewChat(sender, request.recipientId)
        } else {
            getExistingChat(sender, request.recipientId)
        }

        validateMessageSending(sender, recipient)

        val encryptionResponse = encryptMessage(request.content)
        val message = createMessage(chat, sender, request, encryptionResponse)
        val savedMessage = saveMessage(message)

        updateChatStatus(chat, sender, savedMessage)
        notifyRecipient(recipient, sender)

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

    fun getChatByUsers(sessionUser: User, receiverId: Long) : ChatResponse {
        val receiver = userService.getUserByIdFull(receiverId)
        val chat = chatRepository.findChatBetweenUsers(sessionUser, receiver)
            ?: throw ResourceNotFoundException("Chat not found")

        return ChatResponse.from(chat, sessionUser, null)

    }

    private suspend fun decryptedMessage(message: Message, sessionUser: User): MessageResponse {
        val decryptedContent = if (message.encryptedContent != null) {
            try {
                val decryptionResponse = webClient.post()
                    .uri("http://localhost:9002/v1/crypto/decrypt")
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

    private suspend fun createNewChat(sender: User, recipientId: Long): Pair<Chat, User> {
        val otherUser = withContext(Dispatchers.IO) {
            userRepository.findById(recipientId)
        }.orElseThrow { ResourceNotFoundException("Other user not found") }

        val existingChat = withContext(Dispatchers.IO) {
            chatRepository.findChatBetweenUsers(sender, otherUser)
        }

        if (existingChat != null) {
            throw BadRequestException("Invalid Request")
        }

        val chatPayload = Chat(
            user1 = sender,
            user2 = otherUser,
            user1SeenStatus = true,
            user2SeenStatus = false
        )
        val chat = withContext(Dispatchers.IO) {
            chatRepository.save(chatPayload)
        }

        return Pair(chat, otherUser)
    }

    private suspend fun getExistingChat(sender: User, receiverId: Long): Pair<Chat, User> {
        val receiver = userService.getUserByIdFull(receiverId)
        val chat = withContext(Dispatchers.IO) {
            chatRepository.findChatBetweenUsers(sender, receiver)
                ?: throw ResourceNotFoundException("Not found")

        }

        if (chat.user1.id != sender.id && chat.user2.id != sender.id) {
            throw NotAllowedException("You don't have access to this chat")
        }

        val recipient = if (chat.user1.id == sender.id) chat.user2 else chat.user1
        return Pair(chat, recipient)
    }

    private fun validateMessageSending(sender: User, recipient: User) {
        if (recipient.inboxLocked && !isFollowing(sender.id, recipient.id)) {
            throw NotAllowedException("Cannot send message to this user")
        }
    }

    private suspend fun encryptMessage(content: String): EncryptionResponse {
        return try {
            webClient.post()
                .uri("http://localhost:9002/v1/crypto/encrypt")
                .bodyValue(mapOf("message" to content))
                .retrieve()
                .awaitBody()
        } catch (e: Exception) {
            throw RuntimeException("Failed to encrypt message: ${e.message}")
        }
    }

    private fun createMessage(chat: Chat, sender: User, request: SendMessageRequest, encryptionResponse: EncryptionResponse): Message {
        return Message(
            chat = chat,
            sender = sender,
            messageType = request.messageType,
            encryptedContent = if (request.content.isNotBlank()) Base64.getDecoder().decode(encryptionResponse.encryptedMessage) else null,
            encryptedDataKey = Base64.getDecoder().decode(encryptionResponse.encryptedDataKey),
            encryptionVersion = encryptionResponse.keyVersion,
            isOneTime = request.isOneTime
        )
    }

    private suspend fun saveMessage(message: Message): Message {
        return withContext(Dispatchers.IO) {
            messageRepository.save(message)
        }
    }

    private suspend fun updateChatStatus(chat: Chat, sender: User, lastMessage: Message) {
        chat.updatedAt = Instant.now()
        chat.lastMessage = lastMessage
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
    }

    private suspend fun notifyRecipient(recipient: User, sender: User) {
        val content = "New message from ${sender.username}"
        notificationService.createNotification(recipient.id, NotificationTypeEnum.NEW_MESSAGE.toString().lowercase(), content)
    }
}

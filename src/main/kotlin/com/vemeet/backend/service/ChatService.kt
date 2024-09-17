package com.vemeet.backend.service

import com.vemeet.backend.dto.ChatResponse
import com.vemeet.backend.dto.MessageResponse
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
) {
    @Transactional
    fun sendMessage(sender: User, chatId: Long, request: SendMessageRequest): MessageResponse {
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
        chat.lastMessage = savedMessage
        if (sender.id == chat.user1.id) {
            chat.user1SeenStatus = true
            chat.user2SeenStatus = false
        } else {
            chat.user1SeenStatus = false
            chat.user2SeenStatus = true
        }
        chatRepository.save(chat)

        return decryptedMessage(savedMessage, sender)
    }

    fun getUserChats(user: User): List<ChatResponse> {
        val chatsWithLastMessages = chatRepository.findChatsWithLastMessageByUserId(user.id)
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
        val messages = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable)
        return messages.map { decryptedMessage(it, user) }
    }


    private fun decryptedMessage(message: Message, sessionUser: User): MessageResponse {
        val decryptedContent = if (message.encryptedContent != null && message.encryptionType != null) {
            encryptionService.decrypt(
                EncryptedData(
                    encryptedContent = message.encryptedContent,
                    encryptionType = message.encryptionType,
                    encryptedDataKey = ByteBuffer.wrap(message.encryptedDataKey ?: ByteArray(0)),
                    iv = message.encryptionIv ?: ByteArray(0)
                )
            )
        } else {
            null
        }

        return MessageResponse.from(message, decryptedContent, sessionUser)
    }


    @Transactional
    fun createChat(user: User, otherUserId: Long): ChatResponse {
        val otherUser = userRepository.findById(otherUserId).orElseThrow { ResourceNotFoundException("Other user not found") }

        val existingChat = chatRepository.findChatBetweenUsers(user, otherUser)
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
        val savedChat = chatRepository.save(newChat)

        return ChatResponse.from(savedChat, user, lastMessage = null)
    }

    private fun isFollowing(followerId: Long, followedId: Long): Boolean {
        return true // Placeholder
    }

     private fun updateSeenStatus(chat: Chat, user: User) {
        if (user.id == chat.user1.id && !chat.user1SeenStatus) {
            chat.user1SeenStatus = true
            chatRepository.save(chat)
        } else if (user.id == chat.user2.id && !chat.user2SeenStatus) {
            chat.user2SeenStatus = true
            chatRepository.save(chat)
        }
    }
}

package com.vemeet.backend.service

import com.vemeet.backend.dto.ChatDTO
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

        return createMessageDTO(savedMessage)
    }
    fun getUserChats(userId: Long): List<ChatDTO> {
        val chats = chatRepository.findByUser1IdOrUser2Id(userId, userId)
        return chats.map { createChatDTO(it) }
    }

    fun getChatMessages(chatId: Long, userId: Long): List<MessageDTO> {
        val chat = chatRepository.findById(chatId).orElseThrow { ResourceNotFoundException("Chat not found") }
        if (chat.user1.id != userId && chat.user2.id != userId) {
            throw NotAllowedException("You don't have access to this chat")
        }
        val messages = messageRepository.findByChatIdOrderByCreatedAtDesc(chatId)
        return messages.map { createMessageDTO(it) }
    }

    private fun createChatDTO(chat: Chat): ChatDTO {
        return ChatDTO(
            id = chat.id,
            user1 = chat.user1,
            user2 = chat.user2,
            createdAt = chat.createdAt,
            updatedAt = chat.updatedAt
        )
    }

    private fun createMessageDTO(message: Message): MessageDTO {
        val decryptedContent = encryptionService.decrypt(
            EncryptedData(
                encryptedContent = message.encryptedContent ?: ByteArray(0),
                encryptionType = message.encryptionType,
                encryptedDataKey = ByteBuffer.wrap(message.encryptedDataKey ?: ByteArray(0)),
                iv = message.encryptionIv ?: ByteArray(0)
            )
        )

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

    @Transactional
    fun createChat(userId: Long, otherUserId: Long): ChatDTO {
        val user = userRepository.findById(userId).orElseThrow { ResourceNotFoundException("User not found") }
        val otherUser = userRepository.findById(otherUserId).orElseThrow { ResourceNotFoundException("Other user not found") }

        val existingChat = chatRepository.findChatBetweenUsers(user, otherUser)
        if (existingChat != null) {
            return createChatDTO(existingChat)
        }

        val newChat = Chat(user1 = user, user2 = otherUser)
        val savedChat = chatRepository.save(newChat)
        return createChatDTO(savedChat)
    }


    private fun isFollowing(followerId: Long, followedId: Long): Boolean {
        return true // Placeholder
    }


}

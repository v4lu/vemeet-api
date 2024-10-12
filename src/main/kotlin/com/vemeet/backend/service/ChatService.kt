package com.vemeet.backend.service

import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.*
import com.vemeet.backend.repository.ChatAssetRepository
import com.vemeet.backend.repository.ChatRepository
import com.vemeet.backend.repository.MessageRepository
import com.vemeet.backend.repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.apache.coyote.BadRequestException
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.util.*

@Service
class ChatService(
    private val messageRepository: MessageRepository,
    private val userRepository: UserRepository,
    private val chatRepository: ChatRepository,
    private val chatAssetRepository: ChatAssetRepository,
    private val userService: UserService,
    private val chatWebSocketService: ChatWebSocketService,
    private val notificationService: NotificationService,
    private val cryptoService: CryptoService
) {

    @Transactional
    suspend fun sendMessage(sender: User, request: SendMessageRequest): MessageResponse {
        if (sender.id == request.recipientId) {
            throw BadRequestException("You cannot create a chat with yourself")
        }
        val (chat, recipient) = if (request.firstTime) {
            createNewChat(sender, request.recipientId)
        } else {
            getExistingChat(sender, request.recipientId)
        }

        val encryptionResponse = cryptoService.encrypt(request.content)
        val message = createMessage(chat, sender, request, encryptionResponse)
        val savedMessage = withContext(Dispatchers.IO) {
            messageRepository.save(message)
        }

        val chatAssets = request.chatAssets?.map { assetRequest ->
            createAndSaveChatAsset(savedMessage, chat, assetRequest)
        }

        updateChatStatus(chat, sender, savedMessage)
        notifyRecipient(recipient, sender)

        val res = decryptedMessage(savedMessage, sender, chatAssets)
        chatWebSocketService.sendMessage(recipient.id, res)
        return res
    }

    suspend fun getUserChats(user: User): List<ChatResponse> {
        val chatsWithLastMessages = withContext(Dispatchers.IO) {
            chatRepository.findChatsWithLastMessageByUserId(user.id)
        }
        return chatsWithLastMessages.map { chatWithLastMessage ->
            val lastMessage = chatWithLastMessage.lastMessage
            val chatAssets = lastMessage?.let { chatAssetRepository.findAllByMessageId(it.id) }
            ChatResponse.from(
                chat = chatWithLastMessage.chat,
                sessionUser = user,
                lastMessage = lastMessage?.let { decryptedMessage(it, user, chatAssets) }
            )
        }
    }

    suspend fun getChatMessages(chatId: Long, user: User, page: Int, size: Int): Page<MessageResponse> {
        val chat = withContext(Dispatchers.IO) {
            chatRepository.findById(chatId)
        }.orElseThrow { ResourceNotFoundException("Chat not found") }
        if (chat.user1.id != user.id && chat.user2.id != user.id) {
            throw NotAllowedException("You don't have access to this chat")
        }
        updateSeenStatus(chat, user)

        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val messagesPage = withContext(Dispatchers.IO) {
            messageRepository.findByChatIdOrderByCreatedAtDesc(chatId, pageable)
        }

        val decryptedMessages = messagesPage.content.map { message ->
            val chatAssets = chatAssetRepository.findAllByMessageId(message.id)
            decryptedMessage(message, user, chatAssets)
        }

        return PageImpl(decryptedMessages, pageable, messagesPage.totalElements)
    }

    fun getChatByUsers(sessionUser: User, receiverId: Long) : ChatResponse {
        val receiver = userService.getUserByIdFull(receiverId)
        val chat = chatRepository.findChatBetweenUsers(sessionUser, receiver)
            ?: throw ResourceNotFoundException("Chat not found")

        return ChatResponse.from(chat, sessionUser, null)

    }


    private suspend fun decryptedMessage(message: Message, sessionUser: User, chatAssets: List<ChatAsset>?): MessageResponse {
        val decryptedContent = if (message.encryptedContent != null) {
            try {
                message.encryptedDataKey?.let { cryptoService.decrypt(message.encryptedContent, it) }
            } catch (e: Exception) {
                throw RuntimeException("Failed to decrypt message: ${e.message}")
            }
        } else {
            null
        }

        val decryptedFileUrls = chatAssets?.map { decryptFileUrl(it) }

        return MessageResponse.from(message, decryptedContent, sessionUser, chatAssets, decryptedFileUrls)
    }


    suspend fun getChatAssets(chatId: Long, user: User, assetTypes: List<String>, page: Int, size: Int): Page<ChatAssetResponse> {
        val chat = withContext(Dispatchers.IO) {
            chatRepository.findById(chatId)
        }.orElseThrow { ResourceNotFoundException("Chat not found") }
        if (chat.user1.id != user.id && chat.user2.id != user.id) {
            throw NotAllowedException("You don't have access to this chat")
        }

        val pageable = PageRequest.of(page, size, Sort.by("createdAt").descending())
        val assetsPage = withContext(Dispatchers.IO) {
            chatAssetRepository.findByChatIdAndFileTypeIn(chatId, assetTypes, pageable)
        }

        val assetResponses = assetsPage.content.map { chatAsset ->
            val decryptedFileUrl = decryptFileUrl(chatAsset)
            ChatAssetResponse.from(chatAsset, decryptedFileUrl)
        }

        return PageImpl(assetResponses, pageable, assetsPage.totalElements)
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

    private suspend fun createAndSaveChatAsset(message: Message, chat: Chat, assetRequest: ChatAssetRequest): ChatAsset {
        val filePathEncryptionResponse =  cryptoService.encrypt(assetRequest.assetUrl)
        val chatAsset = ChatAsset(
            message = message,
            chat = chat,
            fileType = assetRequest.fileType,
            fileSize = assetRequest.fileSize,
            mimeType = assetRequest.mimeType,
            durationSeconds = assetRequest.durationSeconds,
            encryptedFilePath = Base64.getDecoder().decode(filePathEncryptionResponse.encryptedMessage),
            filePathEncryptedDataKey = Base64.getDecoder().decode(filePathEncryptionResponse.encryptedDataKey),
            filePathEncryptionVersion = filePathEncryptionResponse.keyVersion
        )
        return withContext(Dispatchers.IO) {
            chatAssetRepository.save(chatAsset)
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

    private suspend fun decryptFileUrl(chatAsset: ChatAsset): String? {
        return if (chatAsset.encryptedFilePath != null && chatAsset.filePathEncryptedDataKey != null) {
            try {
                cryptoService.decrypt(chatAsset.encryptedFilePath, chatAsset.filePathEncryptedDataKey)
            } catch (e: Exception) {
                throw RuntimeException("Failed to decrypt file URL: ${e.message}")
            }
        } else {
            null
        }
    }

    private suspend fun notifyRecipient(recipient: User, sender: User) {
        val content = "New message from ${sender.username}"
        notificationService.createNotification(recipient.id, NotificationTypeEnum.NEW_MESSAGE.toString().lowercase(), content)
    }
}

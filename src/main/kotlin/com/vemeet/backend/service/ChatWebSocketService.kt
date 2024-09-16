package com.vemeet.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vemeet.backend.dto.MessageDTO
import com.vemeet.backend.model.User
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.time.Duration
import java.time.Instant

@Service
class ChatWebSocketService(private val objectMapper: ObjectMapper) : TextWebSocketHandler() {
    private val sessions = ConcurrentHashMap<Long, WebSocketSession>()
    private val logger = LoggerFactory.getLogger(ChatWebSocketService::class.java)
    private val sessionTimeouts = ConcurrentHashMap<Long, Long>()
    private val SESSION_TIMEOUT = 5 * 60 * 1000 // 5 minutes in milliseconds
    private val MAX_CONNECTIONS_PER_USER = 5
    private val userConnectionCount = ConcurrentHashMap<Long, AtomicInteger>()
    private val scheduler = Executors.newSingleThreadScheduledExecutor()
    private val undeliveredMessages = ConcurrentHashMap<Long, ConcurrentLinkedQueue<String>>()
    private val MESSAGE_QUEUE_CLEANUP_INTERVAL = 24L * 60 * 60 * 1000 // 24 hours
    private val MAX_MESSAGE_AGE = 7L * 24 * 60 * 60 * 1000 // 7 days

    init {
        scheduler.scheduleAtFixedRate(this::pingAllSessions, 0, 30, TimeUnit.SECONDS)
        scheduler.scheduleAtFixedRate(this::checkSessionTimeouts, 0, 1, TimeUnit.MINUTES)
        scheduler.scheduleAtFixedRate(this::cleanupOldQueuedMessages, MESSAGE_QUEUE_CLEANUP_INTERVAL, MESSAGE_QUEUE_CLEANUP_INTERVAL, TimeUnit.MILLISECONDS)
    }

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? Long
        if (userId != null) {
            val count = userConnectionCount.computeIfAbsent(userId) { AtomicInteger(0) }
            if (count.incrementAndGet() <= MAX_CONNECTIONS_PER_USER) {
                sessions[userId] = session
                sessionTimeouts[userId] = System.currentTimeMillis()
                logger.info("WebSocket connection established for user $userId. Total active connections: ${sessions.size}")

                // Send any queued messages
                sendQueuedMessages(userId, session)
            } else {
                count.decrementAndGet()
                session.close(CloseStatus.POLICY_VIOLATION.withReason("Max connections reached"))
                logger.warn("Max connections reached for user $userId")
            }
        } else {
            logger.error("Failed to establish WebSocket connection: userId is null")
            session.close(CloseStatus.BAD_DATA.withReason("Invalid userId"))
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = session.attributes["userId"] as? Long
        if (userId != null) {
            sessions.remove(userId)
            sessionTimeouts.remove(userId)
            userConnectionCount[userId]?.decrementAndGet()
            logger.info("WebSocket connection closed for user $userId. Status: ${status.code} ${status.reason}. Remaining connections: ${sessions.size}")
        } else {
            logger.error("Failed to close WebSocket connection: userId is null")
        }
    }

    fun broadcastMessage(senderId: Long, message: String) {
        val messageJson = objectMapper.readValue(message, MessageDTO::class.java)
        val recipientId = messageJson.chatId // Assuming chatId represents the recipient

        sendMessage(recipientId, message)
        sendMessage(senderId, message)

        logger.info("Message broadcasted from user $senderId to user $recipientId")
    }


    fun sendMessage(recipientId: Long, message: String) {
        val session = sessions[recipientId]
        if (session != null && session.isOpen) {
            try {
                session.sendMessage(TextMessage(message))
                logger.info("Message sent to user $recipientId")
            } catch (e: Exception) {
                logger.error("Error sending message to user $recipientId", e)
                queueMessage(recipientId, message)
            }
        } else {
            logger.warn("User $recipientId is not connected. Queueing message.")
            queueMessage(recipientId, message)
        }
    }

    private fun queueMessage(recipientId: Long, message: String) {
        undeliveredMessages.computeIfAbsent(recipientId) { ConcurrentLinkedQueue() }.add(message)
        logger.info("Message queued for user $recipientId")
    }
    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val userId = session.attributes["userId"] as? Long
        userId?.let { sessionTimeouts[it] = System.currentTimeMillis() }
        try {
            val jsonNode = objectMapper.readTree(message.payload)
            when (jsonNode["type"].asText()) {
                "ping" -> session.sendMessage(TextMessage("{\"type\":\"pong\"}"))
                "pong" -> {} // Do nothing, just acknowledge
                "message" -> {
                    userId?.let { broadcastMessage(it, message.payload) }
                }
                "get_queued_messages" -> {
                    val userId = session.attributes["userId"] as Long
                    sendQueuedMessages(userId, session)
                }
                else -> {
                    logger.warn("Unexpected message type received: ${jsonNode["type"].asText()}")
                }
            }
        } catch (e: Exception) {
            logger.error("Error handling message: ${e.message}")
        }
    }


    private fun pingAllSessions() {
        sessions.forEach { (userId, session) ->
            try {
                if (session.isOpen) {
                    session.sendMessage(TextMessage("{\"type\":\"ping\"}"))
                } else {
                    sessions.remove(userId)
                    logger.info("Removed closed session for user $userId")
                }
            } catch (e: Exception) {
                logger.error("Error pinging session for user $userId", e)
                sessions.remove(userId)
            }
        }
    }

    private fun checkSessionTimeouts() {
        val currentTime = System.currentTimeMillis()
        sessionTimeouts.forEach { (userId, lastActiveTime) ->
            if (currentTime - lastActiveTime > SESSION_TIMEOUT) {
                sessions[userId]?.let { session ->
                    session.close(CloseStatus.SESSION_NOT_RELIABLE)
                    sessions.remove(userId)
                    sessionTimeouts.remove(userId)
                    logger.info("Closed inactive session for user $userId")
                }
            }
        }
    }

    private fun sendQueuedMessages(userId: Long, session: WebSocketSession) {
        val queue = undeliveredMessages[userId]
        if (queue != null) {
            var messageJson: String? = queue.poll()
            while (messageJson != null) {
                try {
                    val message = parseMessage(messageJson)
                    if (Duration.between(Instant.parse(message.createdAt), Instant.now()).toMillis() <= MAX_MESSAGE_AGE) {
                        session.sendMessage(TextMessage(messageJson))
                        logger.info("Queued message sent to user $userId")
                    } else {
                        logger.info("Discarding old message for user $userId")
                    }
                } catch (e: Exception) {
                    logger.error("Error sending queued message to user $userId", e)
                    queue.add(messageJson) // Re-queue the message if sending fails
                    break
                }
                messageJson = queue.poll()
            }
            if (queue.isEmpty()) {
                undeliveredMessages.remove(userId)
            }
        }
    }

    private fun cleanupOldQueuedMessages() {
        val currentTime = Instant.now()
        undeliveredMessages.forEach { (userId, queue) ->
            queue.removeIf { messageJson ->
                val message = parseMessage(messageJson)
                Duration.between(Instant.parse(message.createdAt), currentTime).toMillis() > MAX_MESSAGE_AGE
            }
            if (queue.isEmpty()) {
                undeliveredMessages.remove(userId)
            }
        }
    }

    private fun parseMessage(messageJson: String): MessageDTO {
        return try {
            objectMapper.readValue(messageJson, MessageDTO::class.java)
        } catch (e: Exception) {
            logger.error("Error parsing message JSON", e)
            // Return a dummy message with a very old timestamp to ensure it gets cleaned up
            MessageDTO(0, 0, User(), "", "", Instant.EPOCH.toString(), null, false)
        }
    }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        val userId = session.attributes["userId"] as? Long
        logger.error("Transport error for user $userId: ${exception.message}", exception)
        session.close(CloseStatus.SERVER_ERROR.withReason("Transport error occurred"))
    }
}
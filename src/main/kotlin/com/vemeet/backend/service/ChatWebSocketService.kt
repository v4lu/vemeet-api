package com.vemeet.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vemeet.backend.dto.SendMessageRequest
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

@Service
class ChatWebSocketService(private val objectMapper: ObjectMapper) : TextWebSocketHandler() {
    private val sessions = ConcurrentHashMap<Long, WebSocketSession>()
    private val logger = LoggerFactory.getLogger(ChatWebSocketService::class.java)
    private val sessionTimeouts = ConcurrentHashMap<Long, Long>()
    private val SESSION_TIMEOUT = 5 * 60 * 1000 // 5 minutes in milliseconds
    private val MAX_CONNECTIONS_PER_USER = 5
    private val userConnectionCount = ConcurrentHashMap<Long, AtomicInteger>()
    private val scheduler = Executors.newSingleThreadScheduledExecutor()

    init {
        scheduler.scheduleAtFixedRate(this::pingAllSessions, 0, 30, TimeUnit.SECONDS)
    }



    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? Long
        if (userId != null) {
            val count = userConnectionCount.computeIfAbsent(userId) { AtomicInteger(0) }
            if (count.incrementAndGet() <= MAX_CONNECTIONS_PER_USER) {
                sessions[userId] = session
                sessionTimeouts[userId] = System.currentTimeMillis()
                logger.info("WebSocket connection established for user $userId. Total active connections: ${sessions.size}")
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
            logger.info("WebSocket connection closed for user $userId. Status: ${status.code} ${status.reason}. Remaining connections: ${sessions.size}")
        } else {
            logger.error("Failed to close WebSocket connection: userId is null")
        }
    }

    fun sendMessage(recipientId: Long, message: String) {
        val session = sessions[recipientId]
        if (session != null && session.isOpen) {
            try {
                session.sendMessage(TextMessage(message))
                logger.info("Message sent to user $recipientId")
            } catch (e: Exception) {
                logger.error("Error sending message to user $recipientId", e)
            }
        } else {
            logger.warn("User $recipientId is not connected")
        }
    }

    override fun handleTextMessage(session: WebSocketSession, message: TextMessage) {
        val userId = session.attributes["userId"] as? Long
        userId?.let { sessionTimeouts[it] = System.currentTimeMillis() }
        try {
            val jsonNode = objectMapper.readTree(message.payload)
            when (jsonNode["type"].asText()) {
                "ping" -> session.sendMessage(TextMessage("{\"type\":\"pong\"}"))
                "pong" -> {} // Do nothing, just acknowledge
                else -> {
                    val messageData = objectMapper.treeToValue(jsonNode, SendMessageRequest::class.java)
                    logger.info("Received message: $messageData")
                    sendMessage(messageData.recipientId, message.payload)
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


    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        val userId = session.attributes["userId"] as? Long
        logger.error("Transport error for user $userId: ${exception.message}", exception)
        session.close(CloseStatus.SERVER_ERROR.withReason("Transport error occurred"))
    }

}

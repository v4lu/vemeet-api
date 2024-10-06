package com.vemeet.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vemeet.backend.dto.MessageResponse
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Service
class ChatWebSocketService(private val objectMapper: ObjectMapper) : TextWebSocketHandler() {
    private val sessions = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()
    private val logger = LoggerFactory.getLogger(ChatWebSocketService::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? Long
        val userIdentifier = session.attributes["userIdentifier"] as? String
        if (userId != null) {
            sessions.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }.add(session)
            logger.info("WebSocket connection established for user $userId ($userIdentifier). Total active connections: ${getTotalConnections()}")
            // Log the current state of sessions for debugging
            logger.debug("Current sessions: {}", sessions.keys)
        } else {
            logger.error("Failed to establish WebSocket connection: userId is null")
            session.close(CloseStatus.BAD_DATA.withReason("Invalid userId"))
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = session.attributes["userId"] as? Long
        val userIdentifier = session.attributes["userIdentifier"] as? String
        if (userId != null) {
            sessions[userId]?.remove(session)
            if (sessions[userId]?.isEmpty() == true) {
                sessions.remove(userId)
            }
            logger.info("WebSocket connection closed for user $userId ($userIdentifier). Reason: ${status.reason ?: "Unknown"}. Total active connections: ${getTotalConnections()}")
        } else {
            logger.error("Failed to close WebSocket connection: userId is null")
        }
    }

    fun sendMessage(recipientId: Long, message: MessageResponse) {
        val userSessions = sessions[recipientId]
        if (!userSessions.isNullOrEmpty()) {
            val messageJson = objectMapper.writeValueAsString(message)
            var sentCount = 0
            userSessions.forEach { session ->
                if (session.isOpen) {
                    try {
                        session.sendMessage(TextMessage(messageJson))
                        sentCount++
                    } catch (e: Exception) {
                        logger.error("Error sending message to user $recipientId on a session", e)
                        // Consider removing the faulty session
                        sessions[recipientId]?.remove(session)
                    }
                } else {
                    // Remove closed sessions
                    sessions[recipientId]?.remove(session)
                    logger.warn("Removed closed session for user $recipientId")
                }
            }
            logger.info("Message sent to $sentCount sessions for user $recipientId. MessageId: ${message.id}")
        } else {
            logger.warn("User $recipientId is not connected. Message not sent. MessageId: ${message.id}")
            // TODO: Implement message queueing for offline users
        }
    }


    @Scheduled(fixedRate = 300000) // Run every 5 minutes
    fun cleanupStaleSessions() {
        sessions.forEach { (userId, userSessions) ->
            userSessions.removeIf { !it.isOpen }
            if (userSessions.isEmpty()) {
                sessions.remove(userId)
            }
        }
        logger.info("Cleaned up stale sessions. Total active connections: ${getTotalConnections()}")
    }

    private fun getTotalConnections(): Int = sessions.values.sumOf { it.size }

    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        val userId = session.attributes["userId"] as? Long
        val userIdentifier = session.attributes["userIdentifier"] as? String
        logger.error("Transport error for user $userId ($userIdentifier)", exception)
    }
}
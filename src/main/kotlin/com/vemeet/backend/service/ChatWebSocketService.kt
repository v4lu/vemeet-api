package com.vemeet.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vemeet.backend.dto.MessageResponse
import org.slf4j.LoggerFactory
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
        if (userId != null) {
            sessions.computeIfAbsent(userId) { ConcurrentHashMap.newKeySet() }.add(session)
        } else {
            session.close(CloseStatus.BAD_DATA.withReason("Invalid userId"))
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = session.attributes["userId"] as? Long
        if (userId != null) {
            sessions[userId]?.remove(session)
            if (sessions[userId]?.isEmpty() == true) {
                sessions.remove(userId)
            }
        }
    }

    fun sendMessage(recipientId: Long, message: MessageResponse) {
        logger.info("Attempting to send message to user: $recipientId")
        val userSessions = sessions[recipientId]
        if (userSessions.isNullOrEmpty()) {
            logger.warn("No active sessions found for user $recipientId")
            return
        }

        val messageJson = objectMapper.writeValueAsString(message)
        var sentCount = 0
        var failedCount = 0

        userSessions.forEach { session ->
            if (session.isOpen) {
                try {
                    session.sendMessage(TextMessage(messageJson))
                    sentCount++
                    logger.debug("Message sent successfully to session ${session.id} for user $recipientId")
                } catch (e: Exception) {
                    failedCount++
                    logger.error("Error sending message to session ${session.id} for user $recipientId", e)
                    sessions[recipientId]?.remove(session)
                }
            } else {
                logger.warn("Session ${session.id} for user $recipientId is closed. Removing from sessions.")
                sessions[recipientId]?.remove(session)
            }
        }

        logger.info("Message sending attempt completed. Sent: $sentCount, Failed: $failedCount, Total sessions: ${userSessions.size}")
    }
}
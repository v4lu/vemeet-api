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

@Service
class ChatWebSocketService(private val objectMapper: ObjectMapper) : TextWebSocketHandler() {
    private val sessions = ConcurrentHashMap<Long, WebSocketSession>()
    private val logger = LoggerFactory.getLogger(ChatWebSocketService::class.java)

    override fun afterConnectionEstablished(session: WebSocketSession) {
        val userId = session.attributes["userId"] as? Long
        if (userId != null) {
            sessions[userId] = session
            logger.info("WebSocket connection established for user $userId")
        } else {
            logger.error("Failed to establish WebSocket connection: userId is null")
            session.close(CloseStatus.BAD_DATA.withReason("Invalid userId"))
        }
    }

    override fun afterConnectionClosed(session: WebSocketSession, status: CloseStatus) {
        val userId = session.attributes["userId"] as? Long
        if (userId != null) {
            sessions.remove(userId)
            logger.info("WebSocket connection closed for user $userId")
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
        try {
            val messageData = objectMapper.readValue(message.payload, SendMessageRequest::class.java)
            logger.info("Received message: $messageData")

            sendMessage(messageData.recipientId, message.payload)
        } catch (e: Exception) {
            logger.error("Error handling message: ${e.message}")
        }
    }
}

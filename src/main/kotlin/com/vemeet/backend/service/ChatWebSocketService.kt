package com.vemeet.backend.service

import com.fasterxml.jackson.databind.ObjectMapper
import com.vemeet.backend.dto.MessageResponse
import org.springframework.stereotype.Service
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.TextMessage
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.handler.TextWebSocketHandler
import java.util.concurrent.ConcurrentHashMap

@Service
class ChatWebSocketService(private val objectMapper: ObjectMapper) : TextWebSocketHandler() {
    private val sessions = ConcurrentHashMap<Long, MutableSet<WebSocketSession>>()

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
                        sessions[recipientId]?.remove(session)
                    }
                } else {
                    sessions[recipientId]?.remove(session)
                }
            }
        }
    }
}
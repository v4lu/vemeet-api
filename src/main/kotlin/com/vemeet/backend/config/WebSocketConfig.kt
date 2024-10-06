package com.vemeet.backend.config
import com.vemeet.backend.service.ChatWebSocketService
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import org.springframework.http.server.ServerHttpRequest
import org.springframework.http.server.ServerHttpResponse
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.config.annotation.EnableWebSocket
import org.springframework.web.socket.config.annotation.WebSocketConfigurer
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {

    private val logger = LoggerFactory.getLogger(WebSocketConfig::class.java)

    @Autowired
    private lateinit var chatWebSocketService: ChatWebSocketService

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatWebSocketService, "/chat")
            .setAllowedOrigins("http://localhost:3000", "http://web-app:3000", "https://app.vemeet.me")
            .addInterceptors(object : HttpSessionHandshakeInterceptor() {
                override fun beforeHandshake(
                    request: ServerHttpRequest,
                    response: ServerHttpResponse,
                    wsHandler: WebSocketHandler,
                    attributes: MutableMap<String, Any>
                ): Boolean {
                    val query = request.uri.query
                    val params = query?.split("&")?.associate {
                        val (key, value) = it.split("=")
                        key to value
                    } ?: emptyMap()

                    val userId = params["userId"]?.toLongOrNull()
                    val token = params["token"]

                    logger.info("WebSocket connection attempt - UserId: $userId, Token: ${token?.take(10)}...")

                    if (userId == null || token == null) {
                        logger.error("WebSocket connection attempt failed: Missing userId or token")
                        return false
                    }

                    // TODO: Implement token validation logic here
                    // For now, we'll just log the attempt
                    logger.info("Token validation would occur here for userId: $userId")

                    attributes["userId"] = userId
                    attributes["userIdentifier"] = token
                    return true
                }
            })
    }
}
package com.vemeet.backend.config
import com.vemeet.backend.service.ChatWebSocketService
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
                    val chatId = params["chatId"]?.toLongOrNull()
                    val token = params["token"]

                    if (userId == null || chatId == null || token == null) {
                        return false
                    }
                    attributes["userId"] = userId
                    attributes["chatId"] = chatId
                    attributes["userIdentifier"] = token
                    return true
                }
            })
    }
}
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
import org.springframework.web.socket.server.HandshakeInterceptor
import org.springframework.web.util.UriComponentsBuilder

@Configuration
@EnableWebSocket
class WebSocketConfig : WebSocketConfigurer {
    @Autowired
    private lateinit var chatWebSocketService: ChatWebSocketService

    override fun registerWebSocketHandlers(registry: WebSocketHandlerRegistry) {
        registry.addHandler(chatWebSocketService, "/chat")
            .setAllowedOrigins("http://localhost:3000", "http://web-app:3000", "https://app.vemeet.me")
            .addInterceptors(object : HandshakeInterceptor {
                override fun beforeHandshake(
                    request: ServerHttpRequest,
                    response: ServerHttpResponse,
                    wsHandler: WebSocketHandler,
                    attributes: MutableMap<String, Any>
                ): Boolean {
                    // Extract userId from query parameters
                    val uri = request.uri
                    val queryParams = UriComponentsBuilder.fromUri(uri).build().queryParams
                    val userId = queryParams.getFirst("userId")?.toLongOrNull()
                    val token = queryParams.getFirst("token")

                    if (userId != null && token != null) {
                        attributes["userId"] = userId
                        attributes["token"] = token
                        return true
                    }
                    return false
                }

                override fun afterHandshake(
                    request: ServerHttpRequest,
                    response: ServerHttpResponse,
                    wsHandler: WebSocketHandler,
                    exception: Exception?
                ) {
                }
            })
    }
}

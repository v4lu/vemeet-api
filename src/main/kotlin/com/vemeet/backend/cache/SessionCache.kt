package com.vemeet.backend.cache

import com.vemeet.backend.model.User
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class SessionCache(private val redisTemplate: RedisTemplate<String, User>) {

    companion object {
        private const val USER_SESSION_PREFIX = "user_session:"

    }

    fun cacheUserSession(token: String, expiresIn: Long, user: User) {
        val key = "$USER_SESSION_PREFIX$token"
        redisTemplate.opsForValue().set(key, user, Duration.ofSeconds(expiresIn))
    }

    fun getUserSession(token: String): User? {
        val key = "$USER_SESSION_PREFIX$token"
        return redisTemplate.opsForValue().get(key)
    }

    fun deleteUserSession(token: String) {
        val key = "$USER_SESSION_PREFIX$token"
        redisTemplate.delete(key)
    }
}
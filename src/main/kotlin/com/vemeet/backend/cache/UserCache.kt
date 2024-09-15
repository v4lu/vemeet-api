package com.vemeet.backend.cache

import com.vemeet.backend.model.User
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class UserCache(private val redisTemplate: RedisTemplate<String, User>) {

    companion object {
        private const val USER_SESSION_PREFIX = "user:"

    }

    fun cacheUser(id: Long, expiresIn: Long, user: User) {
        val key = "$USER_SESSION_PREFIX$id"
        redisTemplate.opsForValue().set(key, user, Duration.ofSeconds(expiresIn))
    }

    fun getUser(id: Long): User? {
        val key = "$USER_SESSION_PREFIX$id"
        return redisTemplate.opsForValue().get(key)
    }

    fun deleteUser(id: Long) {
        val key = "$USER_SESSION_PREFIX$id"
        redisTemplate.delete(key)
    }
}
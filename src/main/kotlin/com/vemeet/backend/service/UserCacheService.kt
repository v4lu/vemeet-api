package com.vemeet.backend.service

import com.vemeet.backend.model.User
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class UserCacheService(private val redisTemplate: RedisTemplate<String, Any>) {

    private val userInfoCacheKey = "user:"

    fun setUserByToken(token: String, expiresIn: Long, user: User) {
        val key = userInfoCacheKey + token
        redisTemplate.opsForValue().set(key, user, Duration.ofSeconds(expiresIn))
    }

    fun getUserByToken(token: String): User? {
        val key = userInfoCacheKey + token
        return redisTemplate.opsForValue().get(key) as? User
    }

    fun deleteUserByToken(token: String) {
        val key = userInfoCacheKey + token
        redisTemplate.delete(key)
    }
}
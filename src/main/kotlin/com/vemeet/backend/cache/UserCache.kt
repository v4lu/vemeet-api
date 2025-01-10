package com.vemeet.backend.cache

import com.vemeet.backend.model.User
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import java.time.Duration

@Service
class UserCache(private val redisTemplate: RedisTemplate<String, User>) {

    companion object {
        private const val USER_AWS_PREFIX = "user_aws:"
        private const val USER_ID_PREFIX = "user_id:"
    }

    fun cacheAWSUser(id: String, expiresIn: Long, user: User) {
        val key = "$USER_AWS_PREFIX$id"
        redisTemplate.opsForValue().set(key, user, Duration.ofSeconds(expiresIn))
    }

    fun getAWSUser(id: String): User? {
        val key = "$USER_AWS_PREFIX$id"
        return redisTemplate.opsForValue().get(key)
    }

    fun deleteAWSUser(id: String) {
        val key = "$USER_AWS_PREFIX$id"
        redisTemplate.delete(key)
    }

    fun cacheIDUser(id: Long, expiresIn: Long, user: User) {
        val key = "$USER_ID_PREFIX$id"
        redisTemplate.opsForValue().set(key, user, Duration.ofSeconds(expiresIn))
    }

    fun getIDUser(id: Long): User? {
        val key = "$USER_ID_PREFIX$id"
        return redisTemplate.opsForValue().get(key)
    }

    fun deleteIDUser(id: Long) {
        val key = "$USER_ID_PREFIX$id"
        redisTemplate.delete(key)
    }

    fun getIDUsers(ids: List<Long>): Map<Long, User> {
        val keys = ids.map { "$USER_ID_PREFIX$it" }
        val users = redisTemplate.opsForValue().multiGet(keys) ?: emptyList()
        return ids.zip(users).mapNotNull { (id, user) ->
            user?.let { id to it }
        }.toMap()
    }
}
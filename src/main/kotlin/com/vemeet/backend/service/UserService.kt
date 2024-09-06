package com.vemeet.backend.service

import com.vemeet.backend.cache.UserCache
import com.vemeet.backend.model.User
import com.vemeet.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.vemeet.backend.dto.UserUpdateRequest
import com.vemeet.backend.exception.ConflictException
import org.springframework.dao.DataIntegrityViolationException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userCache: UserCache
) {
    fun getSessionUser(accessToken: String): User {
        return userCache.getUserSession(accessToken)
            ?: throw Exception("User not found in cache")
    }

    @Transactional
    fun updateUser(accessToken: String, userUpdateRequest: UserUpdateRequest): User {
        val user = getSessionUser(accessToken)

        userUpdateRequest.username?.let { newUsername ->
            if (newUsername != user.username) {
                val existingUser = userRepository.findUserByUsername(newUsername)
                if (existingUser != null && existingUser.id != user.id) {
                    throw ConflictException("Username already exists")
                }
            }
            user.username = newUsername
        }


        userUpdateRequest.bio?.let { user.bio = it }
        userUpdateRequest.name?.let { user.name = it }
        userUpdateRequest.gender?.let { user.gender = it }
        userUpdateRequest.birthplaceLat?.let { user.birthplaceLat = it }
        userUpdateRequest.birthplaceLng?.let { user.birthplaceLng = it }
        userUpdateRequest.birthplaceName?.let { user.birthplaceName = it }
        userUpdateRequest.residenceLat?.let { user.residenceLat = it }
        userUpdateRequest.residenceLng?.let { user.residenceLng = it }
        userUpdateRequest.residenceName?.let { user.residenceName = it }
        userUpdateRequest.isPrivate?.let { user.isPrivate = it }
        userUpdateRequest.inboxLocked.let { user.inboxLocked = it }

        try {
            val updatedUser = userRepository.save(user)
            userCache.cacheUserSession(accessToken, 3600, updatedUser) // 1h
            return updatedUser
        } catch (e: DataIntegrityViolationException) {
            if (e.cause?.cause?.message?.contains("users_username_key") == true) {
                throw ConflictException("Username already exists")
            }
            throw e
        }
    }
}
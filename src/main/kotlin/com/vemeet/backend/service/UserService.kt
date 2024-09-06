package com.vemeet.backend.service

import com.vemeet.backend.cache.UserCache
import com.vemeet.backend.model.User
import com.vemeet.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.vemeet.backend.dto.UserUpdateRequest
import com.vemeet.backend.exception.ConflictException
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.Image
import com.vemeet.backend.repository.ImageRepository
import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userCache: UserCache,
    private val imageRepository: ImageRepository
) {
    private val logger = LoggerFactory.getLogger(UserService::class.java)

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

        when {
            userUpdateRequest.newImageUrl != null -> {
                logger.info("Attempting to set new profile image URL: ${userUpdateRequest.newImageUrl}")
                try {
                    val newImage = Image(url = userUpdateRequest.newImageUrl, user = user)
                    val savedImage = imageRepository.save(newImage)
                    user.profileImage = savedImage
                } catch (e: Exception) {
                    logger.error("Error saving new image: ${e.message}", e)
                    throw e
                }
            }
            userUpdateRequest.existingImageId != null -> {
                val existingImage = imageRepository.findById(userUpdateRequest.existingImageId)
                    .orElseThrow { ResourceNotFoundException("Image not found") }
                if (existingImage.user?.id != user.id) {
                    throw NotAllowedException("You don't have permission to use this image")
                }
                user.profileImage = existingImage
            }
        }

        try {
            val updatedUser = userRepository.save(user)
            userCache.cacheUserSession(accessToken, 3600, updatedUser) // 1h
            return updatedUser
        } catch (e: DataIntegrityViolationException) {
            if (e.cause?.cause?.message?.contains("users_username_key") == true) {
                throw ConflictException("Username already exists")
            }
            throw e
        } catch (e: Exception) {
            logger.error("Error saving new user: ${e.message}", e)
            throw e
        }
    }
}
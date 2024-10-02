package com.vemeet.backend.service

import com.vemeet.backend.cache.SessionCache
import com.vemeet.backend.cache.UserCache
import com.vemeet.backend.dto.UserResponse
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

@Service
class UserService(
    private val userRepository: UserRepository,
    private val sessionCache: SessionCache,
    private val imageRepository: ImageRepository,
    private val userCache: UserCache
) {

    fun getSessionUser(cognitoId: String): User {
        return sessionCache.getUserSession(cognitoId) ?: fetchAndCacheUser(cognitoId)
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
                user.username = newUsername
            }
        }

        userUpdateRequest.bio?.let { user.bio = it }
        userUpdateRequest.name?.let { user.name = it }
        userUpdateRequest.gender?.let { user.gender = it }
        userUpdateRequest.countryName?.let { user.countryName = it }
        userUpdateRequest.countryFlag?.let { user.countryFlag = it }
        userUpdateRequest.countryIsoCode?.let { user.countryIsoCode = it }
        userUpdateRequest.countryLat?.let { user.countryLat = it }
        userUpdateRequest.countryLng?.let { user.countryLng = it }
        userUpdateRequest.cityName?.let { user.cityName = it }
        userUpdateRequest.cityLat?.let { user.cityLat = it }
        userUpdateRequest.cityLng?.let { user.cityLng = it }
        userUpdateRequest.isPrivate?.let { user.isPrivate = it }
        userUpdateRequest.inboxLocked.let { user.inboxLocked = it }
        userUpdateRequest.swiperMode.let { user.swiperMode = it }

        when {
            userUpdateRequest.newImageUrl != null -> {
                val newImage = Image(url = userUpdateRequest.newImageUrl, user = user)
                imageRepository.save(newImage)
                user.profileImage = newImage
            }
            userUpdateRequest.existingImageId != null -> {
                val existingImage = imageRepository.findById(userUpdateRequest.existingImageId)
                    .orElseThrow { ResourceNotFoundException("Image not found") }
                if (existingImage.user?.id != user.id) {
                    throw NotAllowedException("You can only use your own images as profile picture")
                }
                user.profileImage = existingImage
            }
        }

        val updatedUser = userRepository.save(user)
        sessionCache.deleteUserSession(accessToken)
        sessionCache.cacheUserSession(accessToken, 3600, updatedUser) // 1h
        return updatedUser
    }

    fun getUserById(userId: Long): UserResponse {
        val cachedUser = userCache.getIDUser(userId)

        if (cachedUser != null) {
            return UserResponse.fromUser(cachedUser)
        }

        val user = userRepository.findUserById(userId)
            ?: throw ResourceNotFoundException("User with $userId not found")

        userCache.cacheIDUser(userId, 3600, user)
        userCache.cacheAWSUser(user.awsCognitoId, 3600, user)

        return UserResponse.fromUser(user)
    }

    private fun fetchAndCacheUser(cognitoId: String): User {
        val user = userRepository.findUserByAwsCognitoId(cognitoId)
            ?: throw ResourceNotFoundException("User not found for Cognito ID: $cognitoId")

        sessionCache.cacheUserSession(cognitoId, 3600, user)

        return user
    }
}
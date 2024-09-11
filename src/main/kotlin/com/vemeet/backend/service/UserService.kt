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

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userCache: UserCache,
    private val imageRepository: ImageRepository
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
        userCache.deleteUserSession(accessToken)
        userCache.cacheUserSession(accessToken, 3600, updatedUser) // 1h
        return updatedUser
    }
}
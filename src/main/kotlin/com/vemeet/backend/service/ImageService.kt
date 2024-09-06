package com.vemeet.backend.service

import com.vemeet.backend.dto.ImageResponse
import com.vemeet.backend.dto.ImageUploadRequest
import com.vemeet.backend.dto.ImageUploadResponse
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.Image
import com.vemeet.backend.repository.ImageRepository
import com.vemeet.backend.repository.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.format.DateTimeFormatter

@Service
class ImageService(
    private val imageRepository: ImageRepository,
    private val userRepository: UserRepository
) {
    @Transactional(readOnly = true)
    fun getImage(imageId: Long, userId: Long): ImageResponse {
        val image = imageRepository.findByIdAndUserId(imageId, userId)
            ?: throw ResourceNotFoundException("Image not found")
        return ImageResponse(image.id, image.url, DateTimeFormatter.ISO_INSTANT.format(image.createdAt))
    }

    @Transactional(readOnly = true)
    fun getAllUserImages(userId: Long): List<ImageResponse> {
        try {
         return   imageRepository.findAllByUserId(userId).map {
            ImageResponse(it.id, it.url, DateTimeFormatter.ISO_INSTANT.format(it.createdAt))
        }
        } catch (ex: Exception) {
            println(ex.message)
            println(ex.message)
            throw ex
        }
    }

    @Transactional
    fun uploadImages(userId: Long, uploadRequests: List<ImageUploadRequest>): ImageUploadResponse {
        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val uploadedImages = uploadRequests.map { request ->
            Image(url = request.url, user = user)
        }

        val savedImages = imageRepository.saveAll(uploadedImages)
        return ImageUploadResponse(savedImages.map { ImageResponse.fromImage(it) })
    }

    @Transactional
    fun deleteImage(imageId: Long, userId: Long) {
        val image = imageRepository.findByIdAndUserId(imageId, userId)
            ?: throw ResourceNotFoundException("Image not found")

        // If this image is the user's profile image, set it to null
        val user = image.user
        if (user?.profileImage?.id == imageId) {
            user.profileImage = null
            userRepository.save(user)
        }

        imageRepository.delete(image)
    }
}
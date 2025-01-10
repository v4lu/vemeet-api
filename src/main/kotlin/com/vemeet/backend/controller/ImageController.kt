package com.vemeet.backend.controller
import com.vemeet.backend.dto.ImageResponse
import com.vemeet.backend.dto.ImageUploadRequest
import com.vemeet.backend.dto.ImageUploadResponse
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.ImageService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.CognitoIdExtractor
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.security.core.Authentication

@RestController
@RequestMapping("/v1/images")
@Tag(name = "Image", description = "Image management API")
class ImageController(
    private val imageService: ImageService,
    private val userService: UserService
) {

    @GetMapping("/{userId}/{imageId}")
    @Operation(summary = "Get an image by ID")
    fun getImage(@PathVariable imageId: Long, @PathVariable userId: Long): ResponseEntity<ImageResponse> {
        val image = imageService.getImage(imageId, userId)
        return ResponseEntity.ok(image)
    }

    @GetMapping("/{userId}")
    @Operation(summary = "Get all images for the current user")
    fun getAllUserImages(@PathVariable userId: Long): ResponseEntity<List<ImageResponse>> {
        val images = imageService.getAllUserImages(userId)
        return ResponseEntity.ok(images)
    }

    @PostMapping
    @Operation(summary = "Upload one or more images")
    fun uploadImages(
        @Valid @RequestBody uploadRequests: List<ImageUploadRequest> ,
        authentication: Authentication
    ): ResponseEntity<ImageUploadResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val response = imageService.uploadImages(user.id, uploadRequests)

        return ResponseEntity(response, HttpStatus.CREATED)
    }

    @DeleteMapping("/{imageId}")
    @Operation(summary = "Delete an image")
    fun deleteImage(@PathVariable imageId: Long, authentication: Authentication): ResponseEntity<Unit> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        imageService.deleteImage(imageId, user.id)
        return ResponseEntity.noContent().build()
    }
}
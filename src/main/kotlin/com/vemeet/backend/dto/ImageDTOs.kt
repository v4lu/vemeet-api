package com.vemeet.backend.dto

import com.vemeet.backend.model.Image
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.format.DateTimeFormatter


@Schema(description = "Image response object")
data class ImageResponse(
    @Schema(description = "Image ID", example = "1")
    val id: Long,

    @Schema(description = "Image URL", example = "https://example.com/image.jpg")
    val url: String,

    @Schema(description = "Image creation timestamp", example = "2024-08-27T10:30:00Z")
    val createdAt: String,
)  {
    companion object {
        fun fromImage(image: Image): ImageResponse {
            return ImageResponse(
                id = image.id,
                url = image.url,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
            )
        }
    }
}


@Schema(description = "Image upload request")
data class ImageUploadRequest(
    @Schema(description = "Image URL", example = "https://example.com/image.jpg", required = true)
    val url: String
)

@Schema(description = "Image upload response")
data class ImageUploadResponse(
    @Schema(description = "List of uploaded image IDs", example = "[1, 2, 3]")
    val imageIds: List<ImageResponse>
)

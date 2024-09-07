package com.vemeet.backend.dto

import com.vemeet.backend.model.LocationImage
import com.vemeet.backend.model.LocationReview
import com.vemeet.backend.model.ReviewImage
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.format.DateTimeFormatter

@Schema(description = "Location Review Response object")
data class LocationReviewResponse(
    @Schema(description = "Review ID", example = "1")
    val id: Long,

    @Schema(description = "User who wrote the review")
    val user: UserResponse,

    @Schema(description = "Rating", example = "4")
    val rating: Int,

    @Schema(description = "Review comment", example = "Great vegan options and friendly staff!")
    val comment: String?,

    @Schema(description = "Creation date", example = "2024-08-27T10:30:00Z")
    val createdAt: String,

    @Schema(description = "Last update date", example = "2024-08-27T10:30:00Z")
    val updatedAt: String,

    @Schema(description = "List of image URLs associated with the review")
    val images: List<ReviewImageResponse>
) {
    companion object {
        fun from(location: LocationReview): LocationReviewResponse {
            return LocationReviewResponse(
                id = location.id,
                user = UserResponse.fromUser(location.user),
                rating = location.rating,
                comment = location.comment,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(location.createdAt),
                updatedAt = DateTimeFormatter.ISO_INSTANT.format(location.updatedAt),
                images = location.images.map { ReviewImageResponse(it) }
            )
        }
    }
}


@Schema(description = "Images" )
data class ReviewImageResponse(
    @Schema(description = "Image id", example = "1")
    val id : Long,

    @Schema(description = "Url of image", example = "https://example.com")
    val url : String,
) {
    constructor(image: ReviewImage) : this(
        id = image.id,
        url = image.imageUrl
    )
}

@Schema(description = "Location Review Request object")
data class LocationReviewRequest(
    @Schema(description = "Rating", example = "4")
    val rating: Int,

    @Schema(description = "Review comment", example = "Great vegan options and friendly staff!")
    val comment: String?,

    @Schema(description = "List of image URLs to add to the review")
    val images: List<String>?
)

@Schema(description = "Location Review Update Request object")
data class LocationReviewUpdateRequest(
    @Schema(description = "Rating", example = "4")
    val rating: Int?,

    @Schema(description = "Review comment", example = "Great vegan options and friendly staff!")
    val comment: String?,

    @Schema(description = "List of image URLs to add to the review")
    val imagesToAdd: List<String>?,

    @Schema(description = "List of image IDs to remove from the review")
    val imageIdsToRemove: List<Long>?
)
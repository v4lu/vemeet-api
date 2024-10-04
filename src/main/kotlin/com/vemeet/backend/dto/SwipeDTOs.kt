package com.vemeet.backend.dto


import com.vemeet.backend.model.PotentialMatch
import com.vemeet.backend.model.Swipe
import com.vemeet.backend.model.SwiperUserProfile
import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant
import java.time.format.DateTimeFormatter

@Schema(description = "Swipe Request object")
data class SwipeRequest(
    @Schema(description = "ID of the user being swiped on", example = "2")
    val swipedUserId: Long,

    @Schema(description = "Direction of the swipe", example = "right")
    val direction: String
)

@Schema(description = "Swipe Response object")
data class SwipeResponse(
    @Schema(description = "Swipe ID", example = "1")
    val id: Long,

    @Schema(description = "ID of the user who swiped", example = "1")
    val swiperId: Long,

    @Schema(description = "ID of the user who was swiped on", example = "2")
    val swipedId: Long,

    @Schema(description = "Direction of the swipe", example = "right")
    val direction: String,

    @Schema(description = "Timestamp of the swipe", example = "2024-09-17T12:30:00Z")
    val createdAt: String,

    @Schema(description = "Whether this swipe resulted in a match", example = "true")
    val isMatch: Boolean
) {
    companion object {
        fun fromSwipe(swipe: Swipe, isMatch: Boolean): SwipeResponse {
            return SwipeResponse(
                id = swipe.id,
                swiperId = swipe.swiper.id,
                swipedId = swipe.swiped.id,
                direction = swipe.direction,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(swipe.createdAt),
                isMatch = isMatch
            )
        }
    }
}


@Schema(description = "SwiperUserProfile Request object")
data class SwiperUserProfileRequest(
    @Schema(description = "User description", example = "I love hiking and photography")
    val description: String?,

    @Schema(description = "URL of the main profile image", example = "https://example.com/image.jpg")
    val mainImageUrl: String?,

    @Schema(description = "List of URLs for other profile images")
    val otherImages: MutableList<String>,
    )

@Schema(description = "SwiperUserProfile Response object")
data class SwiperUserProfileResponse(
    @Schema(description = "Profile ID", example = "1")
    val id: Long,

    @Schema(description = "User ID", example = "2")
    val userId: Long,

    @Schema(description = "User description", example = "I love hiking and photography")
    val description: String?,

    @Schema(description = "URL of the main profile image", example = "https://example.com/image.jpg")
    val mainImageUrl: String?,

    @Schema(description = "List of URLs for other profile images")
    val otherImages: MutableList<String>,

    @Schema(description = "Timestamp of profile creation", example = "2024-09-17T12:30:00Z")
    val createdAt: Instant,

    @Schema(description = "Timestamp of last profile update", example = "2024-09-17T14:45:00Z")
    val updatedAt: Instant,

    @Schema(description = "User details")
    val user: UserResponse
) {
    companion object {
        fun fromSwiperUserProfile(profile: SwiperUserProfile, user: UserResponse ): SwiperUserProfileResponse {
            return SwiperUserProfileResponse(
                id = profile.id,
                userId = profile.userId,
                description = profile.description,
                mainImageUrl = profile.mainImageUrl,
                otherImages = profile.otherImages,
                createdAt = profile.createdAt,
                updatedAt = profile.updatedAt,
                user = user
            )
        }
    }
}

@Schema(description = "SwiperUserProfile Response object")
data class SwiperPotencialUserProfileResponse(
    @Schema(description = "Profile ID", example = "1")
    val id: Long,

    @Schema(description = "User ID", example = "2")
    val userId: Long,

    @Schema(description = "User description", example = "I love hiking and photography")
    val description: String?,

    @Schema(description = "URL of the main profile image", example = "https://example.com/image.jpg")
    val mainImageUrl: String?,

    @Schema(description = "List of URLs for other profile images")
    val otherImages: MutableList<String>,

    @Schema(description = "Timestamp of profile creation", example = "2024-09-17T12:30:00Z")
    val createdAt: Instant,

    @Schema(description = "Timestamp of last profile update", example = "2024-09-17T14:45:00Z")
    val updatedAt: Instant,

    @Schema(description = "How far away is user from the potenctial match", example = "30" )
    val distance: Double,

    @Schema(description = "User details")
    val user: UserResponse
) {
    companion object {
        fun fromSwiperUserProfile(profile: SwiperUserProfile, user: UserResponse, potentialMatch: PotentialMatch): SwiperPotencialUserProfileResponse {
            return SwiperPotencialUserProfileResponse(
                id = profile.id,
                userId = profile.userId,
                description = profile.description,
                mainImageUrl = profile.mainImageUrl,
                otherImages = profile.otherImages,
                createdAt = profile.createdAt,
                updatedAt = profile.updatedAt,
                user = user,
                distance = potentialMatch.distance,
            )
        }
    }
}
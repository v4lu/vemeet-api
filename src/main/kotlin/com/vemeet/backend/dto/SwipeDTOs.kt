package com.vemeet.backend.dto


import com.vemeet.backend.model.Swipe
import io.swagger.v3.oas.annotations.media.Schema
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

@Schema(description = "Potential Match Response object")
data class PotentialMatchResponse(
    @Schema(description = "User ID", example = "2")
    val userId: Long,

    @Schema(description = "Distance from the current user", example = "5.2")
    val distance: Double,

    @Schema(description = "User details")
    val user: UserResponse
)

data class PaginatedPotentialMatches(
    val matches: List<PotentialMatchResponse>,
    val hasNextPage: Boolean
)

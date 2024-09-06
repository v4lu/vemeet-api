package com.vemeet.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.Instant

@Schema(description = "Follow request object")
data class FollowRequest(
    @Schema(description = "ID of the user who wants to follow", example = "1")
    val followerId: Long,

    @Schema(description = "ID of the user to be followed", example = "2")
    val followedId: Long
)

@Schema(description = "Follow response object")
data class FollowResponse(
    @Schema(description = "ID of the follow relationship", example = "1")
    val id: Long,

    @Schema(description = "Username of the follower", example = "johndoe")
    val followerUsername: String,

    @Schema(description = "Username of the followed user", example = "janedoe")
    val followedUsername: String,

    @Schema(description = "Timestamp of when the follow relationship was created", example = "2023-09-06T12:00:00Z")
    val createdAt: Instant
)

@Schema(description = "User follow statistics response")
data class UserFollowStatsResponse(
    @Schema(description = "ID of the user", example = "1")
    val userId: Long,

    @Schema(description = "Username of the user", example = "johndoe")
    val username: String,

    @Schema(description = "Number of followers the user has", example = "100")
    val followerCount: Int,

    @Schema(description = "Number of users the user is following", example = "50")
    val followingCount: Int
)

@Schema(description = "Follow request response object")
data class FollowRequestResponse(
    @Schema(description = "ID of the follow request", example = "1")
    val id: Long,

    @Schema(description = "Username of the requester", example = "johndoe")
    val requesterUsername: String,

    @Schema(description = "Username of the target user", example = "janedoe")
    val targetUsername: String,

    @Schema(description = "Timestamp of when the follow request was created", example = "2023-09-06T12:00:00Z")
    val createdAt: String,
)

@Schema(description = "Message Follow response")
data class MessageFollowResponse(
    val message: String,
)
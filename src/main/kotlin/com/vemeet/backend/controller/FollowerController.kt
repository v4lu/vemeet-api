package com.vemeet.backend.controller

import com.vemeet.backend.dto.*
import com.vemeet.backend.service.FollowerService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.extractAccessToken
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/followers")
@Tag(name = "Follower", description = "Follower management APIs")
class FollowerController(
    private val followerService: FollowerService,
    private val userService: UserService,
) {

    @PostMapping("/follow/{followId}")
    @Operation(summary = "Follow a user", description = "Creates a new follower relationship")
    @ApiResponse(responseCode = "201", description = "Successfully followed the user",
        content = [Content(schema = Schema(implementation = MessageFollowResponse::class))])
    fun followUser(
        @PathVariable followId: Long,
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<MessageFollowResponse> {
        val accessToken = extractAccessToken(authHeader)
        val user = userService.getSessionUser(accessToken)
        val response = followerService.followUser(user, followId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/unfollow/{unfollowId}")
    @Operation(summary = "Unfollow a user", description = "Removes an existing follower relationship")
    @ApiResponse(responseCode = "204", description = "Successfully unfollowed the user")
    fun unfollowUser(
        @PathVariable unfollowId: Long,
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<Unit> {
        val accessToken = extractAccessToken(authHeader)
        val user = userService.getSessionUser(accessToken)
        followerService.unfollowUser(user, unfollowId)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/followers/{userId}")
    @Operation(summary = "Get user's followers", description = "Retrieves a list of users who follow the specified user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of followers",
        content = [Content(schema = Schema(implementation = UserResponse::class))])
    fun getUserFollowers(@Parameter(description = "ID of the user") @PathVariable userId: Long): ResponseEntity<List<UserResponse>> {
        val followers = followerService.getUserFollowers(userId)
        return ResponseEntity.ok(followers)
    }

    @GetMapping("/following/{userId}")
    @Operation(summary = "Get who user follow", description = "Retrieves a list of users followed by the specified user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of followed users",
        content = [Content(schema = Schema(implementation = UserResponse::class))])
    fun getUserFollowing(@Parameter(description = "ID of the user") @PathVariable userId: Long): ResponseEntity<List<UserResponse>> {
        val following = followerService.getUserFollowing(userId)
        return ResponseEntity.ok(following)
    }

    @GetMapping("/stats/{userId}")
    @Operation(summary = "Get user's follow statistics", description = "Retrieves follower and following counts for a user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the user's follow statistics",
        content = [Content(schema = Schema(implementation = UserFollowStatsResponse::class))])
    fun getUserFollowStats(@Parameter(description = "ID of the user") @PathVariable userId: Long): ResponseEntity<UserFollowStatsResponse> {
        val stats = followerService.getUserFollowStats(userId)
        return ResponseEntity.ok(stats)
    }

    @PostMapping("/accept-request/{requestId}")
    @Operation(summary = "Accept a follow request", description = "Accepts a pending follow request")
    @ApiResponse(responseCode = "200", description = "Successfully accepted the follow request")
    fun acceptFollowRequest(
        @PathVariable requestId: Long,
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<Unit> {
        val accessToken = extractAccessToken(authHeader)
        val user = userService.getSessionUser(accessToken)
        followerService.acceptFollowRequest(requestId, user)
        return ResponseEntity.ok().build()
    }

    @PostMapping("/reject-request/{requestId}")
    @Operation(summary = "Reject a follow request", description = "Rejects a pending follow request")
    @ApiResponse(responseCode = "200", description = "Successfully rejected the follow request")
    fun rejectFollowRequest(
        @PathVariable requestId: Long,
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<Unit> {
        val accessToken = extractAccessToken(authHeader)
        val user = userService.getSessionUser(accessToken)
        followerService.rejectFollowRequest(requestId, user.id)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/pending-requests")
    @Operation(summary = "Get pending follow requests", description = "Retrieves a list of pending follow requests for the user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of pending follow requests",
        content = [Content(schema = Schema(implementation = FollowRequestResponse::class))])
    fun getPendingFollowRequests(
        @RequestHeader("Authorization") authHeader: String,
    ): ResponseEntity<List<FollowRequestResponse>> {
        val accessToken = extractAccessToken(authHeader)
        val user = userService.getSessionUser(accessToken)
        val pendingRequests = followerService.getPendingFollowRequests(user.id)
        return ResponseEntity.ok(pendingRequests)
    }
}
package com.vemeet.backend.controller

import com.vemeet.backend.dto.*
import com.vemeet.backend.service.FollowerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/followers")
@Tag(name = "Follower", description = "Follower management APIs")
class FollowerController(private val followerService: FollowerService) {

    @PostMapping("/follow")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Follow a user", description = "Creates a new follower relationship")
    @ApiResponse(responseCode = "201", description = "Successfully followed the user",
        content = [Content(schema = Schema(implementation = MessageFollowResponse::class))])
    fun followUser(@RequestBody followRequest: FollowRequest): MessageFollowResponse {
        return followerService.followUser(followRequest.followerId, followRequest.followedId)
    }

    @DeleteMapping("/unfollow")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Unfollow a user", description = "Removes an existing follower relationship")
    @ApiResponse(responseCode = "204", description = "Successfully unfollowed the user")
    fun unfollowUser(@RequestBody followRequest: FollowRequest) {
        followerService.unfollowUser(followRequest.followerId, followRequest.followedId)
    }

    @GetMapping("/followers/{userId}")
    @Operation(summary = "Get user's followers", description = "Retrieves a list of users who follow the specified user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of followers",
        content = [Content(schema = Schema(implementation = UserResponse::class))])
    fun getUserFollowers(@Parameter(description = "ID of the user") @PathVariable userId: Long): List<UserResponse> {
        return followerService.getUserFollowers(userId)
    }

    @GetMapping("/following/{userId}")
    @Operation(summary = "Get users followed by a user", description = "Retrieves a list of users followed by the specified user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of followed users",
        content = [Content(schema = Schema(implementation = UserResponse::class))])
    fun getUserFollowing(@Parameter(description = "ID of the user") @PathVariable userId: Long): List<UserResponse> {
        return followerService.getUserFollowing(userId)
    }

    @GetMapping("/stats/{userId}")
    @Operation(summary = "Get user's follow statistics", description = "Retrieves follower and following counts for a user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the user's follow statistics",
        content = [Content(schema = Schema(implementation = UserFollowStatsResponse::class))])
    fun getUserFollowStats(@Parameter(description = "ID of the user") @PathVariable userId: Long): UserFollowStatsResponse {
        return followerService.getUserFollowStats(userId)
    }

    @PostMapping("/accept-request/{requestId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Accept a follow request", description = "Accepts a pending follow request")
    @ApiResponse(responseCode = "200", description = "Successfully accepted the follow request")
    fun acceptFollowRequest(@PathVariable requestId: Long, @RequestParam userId: Long) {
        followerService.acceptFollowRequest(requestId, userId)
    }

    @PostMapping("/reject-request/{requestId}")
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Reject a follow request", description = "Rejects a pending follow request")
    @ApiResponse(responseCode = "200", description = "Successfully rejected the follow request")
    fun rejectFollowRequest(@PathVariable requestId: Long, @RequestParam userId: Long) {
        followerService.rejectFollowRequest(requestId, userId)
    }

    @GetMapping("/pending-requests")
    @Operation(summary = "Get pending follow requests", description = "Retrieves a list of pending follow requests for the user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the list of pending follow requests",
        content = [Content(schema = Schema(implementation = FollowRequestResponse::class))])
    fun getPendingFollowRequests(@RequestParam userId: Long): List<FollowRequestResponse> {
        return followerService.getPendingFollowRequests(userId)
    }
}


package com.vemeet.backend.controller

import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.StoryService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.CognitoIdExtractor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/stories")
@Tag(name = "Stories", description = "Story management APIs")
class StoryController(
    private val storyService: StoryService,
    private val userService: UserService,
) {

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a new story",
        description = "Creates a new story for the authenticated user",
        responses = [
            ApiResponse(
                responseCode = "201", description = "Successfully created",
                content = [Content(schema = Schema(implementation = StoryResponse::class))]
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized - Invalid token",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Bad Request - Invalid input",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    suspend fun createStory(
        authentication: Authentication,
        @RequestBody request: CreateStoryRequest
    ): ResponseEntity<StoryResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val response = storyService.createStory(user, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get user's stories",
        description = "Retrieves all active stories for a specific user",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved",
                content = [Content(schema = Schema(implementation = Array<StoryResponse>::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun getUserStories(@PathVariable userId: Long): ResponseEntity<List<StoryResponse>> {
        val stories = storyService.getUserStories(userId)
        return ResponseEntity.ok(stories)
    }

    @GetMapping("/{storyId}")
    @Operation(
        summary = "Get story details",
        description = "Retrieves details of a specific story",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved",
                content = [Content(schema = Schema(implementation = StoryResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Story not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun getStoryDetails(@PathVariable storyId: Long): ResponseEntity<StoryResponse> {
        val story = storyService.getStoryDetails(storyId)
        return ResponseEntity.ok(story)
    }

    @PostMapping("/{storyId}/view")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Record story view",
        description = "Records a view for the specified story",
        responses = [
            ApiResponse(
                responseCode = "201", description = "Successfully recorded",
                content = [Content(schema = Schema(implementation = StoryViewResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Story not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized - Invalid token",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun recordStoryView(
        authentication: Authentication,
        @PathVariable storyId: Long
    ): ResponseEntity<StoryViewResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val response = storyService.recordStoryView(user, storyId)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @DeleteMapping("/{storyId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete story",
        description = "Deletes the specified story",
        responses = [
            ApiResponse(responseCode = "204", description = "Successfully deleted"),
            ApiResponse(
                responseCode = "404", description = "Story not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized - Invalid token",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "403", description = "Forbidden - User does not own the story",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun deleteStory(authentication: Authentication, @PathVariable storyId: Long): ResponseEntity<Unit> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        storyService.deleteStory(user, storyId)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/groups")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
        summary = "Create a new story group",
        description = "Creates a new story group for the authenticated user",
        responses = [
            ApiResponse(
                responseCode = "201", description = "Successfully created",
                content = [Content(schema = Schema(implementation = StoryGroupResponse::class))]
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized - Invalid token",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Bad Request - Invalid input",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun createStoryGroup(
        authentication: Authentication,
        @RequestBody request: CreateStoryGroupRequest
    ): ResponseEntity<StoryGroupResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val response = storyService.createStoryGroup(user, request)
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }


    @GetMapping("/groups/{groupId}")
    @Operation(
        summary = "Get story group details",
        description = "Retrieves details of a specific story group",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved",
                content = [Content(schema = Schema(implementation = StoryGroupResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Story group not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun getStoryGroupDetails(@PathVariable groupId: Long): ResponseEntity<StoryGroupResponse> {
        val group = storyService.getStoryGroupDetails(groupId)
        return ResponseEntity.ok(group)
    }

    @GetMapping("/groups/user/{userId}")
    @Operation(
        summary = "Get user's story groups",
        description = "Retrieves all story groups for a specific user",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved",
                content = [Content(schema = Schema(implementation = Array<StoryGroupResponse>::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "User not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun getUserStoryGroups(@PathVariable userId: Long): ResponseEntity<List<StoryGroupResponse>> {
        val groups = storyService.getUserStoryGroups(userId)
        return ResponseEntity.ok(groups)
    }

    @DeleteMapping("/groups/{groupId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
        summary = "Delete story group",
        description = "Deletes the specified story group",
        responses = [
            ApiResponse(responseCode = "204", description = "Successfully deleted"),
            ApiResponse(
                responseCode = "404", description = "Story group not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "401", description = "Unauthorized - Invalid token",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "403", description = "Forbidden - User does not own the story group",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun deleteStoryGroup(authentication: Authentication, @PathVariable groupId: Long): ResponseEntity<Unit> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        storyService.deleteStoryGroup(user, groupId)
        return ResponseEntity.noContent().build()
    }
}
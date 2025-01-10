package com.vemeet.backend.controller

import com.vemeet.backend.dto.ExceptionResponse
import com.vemeet.backend.dto.FeedItemResponse
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.FeedService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.CognitoIdExtractor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springdoc.core.converters.models.PageableAsQueryParam
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/feed")
@Tag(name = "Feed", description = "Feed management APIs")
class FeedController(
    private val feedService: FeedService,
    private val userService: UserService,
) {

    @GetMapping
    @Operation(
        summary = "Get user feed",
        description = """
            Retrieves the feed for the authenticated user. The feed includes both posts and recipes.
            
            Example usage:
            - Basic: /v1/feed
            - With pagination: /v1/feed?page=0&size=10
            - With sorting: /v1/feed?sort=createdAt,desc
            - Combined: /v1/feed?page=0&size=10&sort=createdAt,desc
        """,
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved feed items",
                content = [Content(
                    schema = Schema(implementation = FeedItemResponse::class)
                )]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Invalid or missing token",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "500",
                description = "Internal server error",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    @PageableAsQueryParam
    fun getFeed(
        authentication: Authentication,
        @Parameter(hidden = true) pageable: Pageable
    ): ResponseEntity<Page<FeedItemResponse>> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)
            ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val feed = feedService.getFeedForUser(user.id, pageable)
        return ResponseEntity.ok(feed)
    }
}

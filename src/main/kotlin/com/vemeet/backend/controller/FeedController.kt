package com.vemeet.backend.controller

import com.vemeet.backend.dto.PostResponse
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.FeedService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.CognitoIdExtractor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/feed")
class FeedController(
    private val feedService: FeedService,
    private val userService: UserService,
) {

    @GetMapping
    @Operation(
        summary = "Get user feed",
        description = """
             Example usage:
        - Basic: /v1/feed
        - With search: /v1/feed?search=cafe
        - With pagination: /v1/feed{id}?page=0&size=10
        - With sorting: /v1/feed{id}?sort=name,asc
        - Combined: /v1/feed?search=cafe&page=0&size=10&sort=name,asc&sort=city,desc
        """,
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved posts",
                content = [Content(schema = Schema(implementation = Page::class))]
            )
        ]
    )
    fun getFeed(
        authentication: Authentication,
        pageable: Pageable
    ): ResponseEntity<Page<PostResponse>> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val posts = feedService.getFeedForUser(user.id, pageable)
        return ResponseEntity.ok(posts)
    }
}

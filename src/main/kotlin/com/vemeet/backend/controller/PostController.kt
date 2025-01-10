package com.vemeet.backend.controller

import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.PostService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.CognitoIdExtractor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/posts")
@Tag(name = "Post", description = "Post endpoints")
class PostController(
    private val postService: PostService,
    private val userService: UserService,
) {

    @GetMapping("/{postId}")
    @Operation(
        summary = "Get a post by ID",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved post",
                content = [Content(schema = Schema(implementation = PostResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Post not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "403", description = "Not allowed to view this post",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun getPost(
        @PathVariable postId: Long,
        authentication: Authentication,
    ): ResponseEntity<PostResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val currentUser = userService.getSessionUser(cognitoId)
        val post = postService.getPostById(postId, currentUser)
        return ResponseEntity.ok(post)
    }

    @GetMapping
    @Operation(
        summary = "Get visible posts",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved posts",
                content = [Content(schema = Schema(implementation = Page::class))]
            )
        ]
    )
    fun getVisiblePosts(
        authentication: Authentication,
        pageable: Pageable
    ): ResponseEntity<Page<PostResponse>> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val currentUser = userService.getSessionUser(cognitoId)
        val posts = postService.getVisiblePosts(currentUser, pageable)
        return ResponseEntity.ok(posts)
    }

    @PostMapping
    @Operation(
        summary = "Create a new post",
        responses = [
            ApiResponse(
                responseCode = "201",
                description = "Successfully created post",
                content = [Content(schema = Schema(implementation = PostResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Bad request - Post must have either content or images",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "401",
                description = "Unauthorized - Invalid or missing authentication token",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "422",
                description = "Unprocessable Entity - Validation errors",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun createPost(
        authentication: Authentication,
        @RequestBody request: PostCreateRequest
    ): ResponseEntity<PostResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val currentUser = userService.getSessionUser(cognitoId)
        val createdPost = postService.createPost(currentUser, request)
        return ResponseEntity.ok(createdPost)
    }

    @PatchMapping("/{postId}")
    @Operation(
        summary = "Update a post",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully updated post",
                content = [Content(schema = Schema(implementation = PostResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Post not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "403", description = "Not allowed to update this post",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun updatePost(
        @PathVariable postId: Long,
        authentication: Authentication,
        @RequestBody request: PostUpdateRequest
    ): ResponseEntity<PostResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val currentUser = userService.getSessionUser(cognitoId)
        val updatedPost = postService.updatePost(postId, currentUser, request)
        return ResponseEntity.ok(updatedPost)
    }

    @DeleteMapping("/{postId}")
    @Operation(
        summary = "Delete a post",
        responses = [
            ApiResponse(responseCode = "204", description = "Successfully deleted post"),
            ApiResponse(
                responseCode = "404", description = "Post not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "403", description = "Not allowed to delete this post",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun deletePost(
        @PathVariable postId: Long,
        authentication: Authentication
    ): ResponseEntity<Unit> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val currentUser = userService.getSessionUser(cognitoId)
        postService.deletePost(postId, currentUser)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{postId}/reactions")
    @Operation(
        summary = "Add a reaction to a post",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully added reaction",
                content = [Content(schema = Schema(implementation = PostResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Post not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Currently only likes are allowed",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "403", description = "Not allowed to react to this post",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun addReaction(
        @PathVariable postId: Long,
        authentication: Authentication,
        @RequestBody request: ReactionCreateRequest
    ): ResponseEntity<PostResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val currentUser = userService.getSessionUser(cognitoId)
        val updatedPost = postService.addReaction(postId, currentUser, request)
        return ResponseEntity.ok(updatedPost)
    }

    @DeleteMapping("/{postId}/reactions")
    @Operation(
        summary = "Remove a reaction from a post",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully removed reaction",
                content = [Content(schema = Schema(implementation = PostResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Post or reaction not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun removeReaction(
        @PathVariable postId: Long,
        authentication: Authentication
    ): ResponseEntity<PostResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val currentUser = userService.getSessionUser(cognitoId)
        val updatedPost = postService.removeReaction(postId, currentUser)
        return ResponseEntity.ok(updatedPost)
    }

    @GetMapping("/session")
    @Operation(
        summary = "Get user posts by session",
        description = """
             Example usage:
        - Basic: /v1/posts/session
        - With search: /v1/posts/session?search=cafe
        - With pagination: /v1/posts/session?page=0&size=10
        - With sorting: /v1/posts/session?sort=name,asc
        - Combined: /v1/posts/session?search=cafe&page=0&size=10&sort=name,asc&sort=city,desc
        """,
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved posts",
                content = [Content(schema = Schema(implementation = Page::class))]
            )
        ]
    )
    fun getSessionPosts(
        authentication: Authentication,
        pageable: Pageable
    ): ResponseEntity<Page<PostResponse>> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")

        val currentUser = userService.getSessionUser(cognitoId)
        val posts = postService.getUserPosts(currentUser.id, pageable)
        return ResponseEntity.ok(posts)
    }


    @GetMapping("/user/{userId}")
    @Operation(
        summary = "Get user posts by user id ",
        description = """
             Example usage:
        - Basic: /v1/posts/session
        - With search: /v1/posts/user/{id}?search=cafe
        - With pagination: /v1/posts/user/{id}?page=0&size=10
        - With sorting: /v1/posts/user/{id}?sort=name,asc
        - Combined: /v1/posts/user/{id}?search=cafe&page=0&size=10&sort=name,asc&sort=city,desc
        """,
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved posts",
                content = [Content(schema = Schema(implementation = Page::class))]
            )
        ]
    )
    fun getUserPosts(
        @PathVariable userId: Long,
        pageable: Pageable
    ): ResponseEntity<Page<PostResponse>> {
        val posts = postService.getUserPosts(userId, pageable)
        return ResponseEntity.ok(posts)
    }
}
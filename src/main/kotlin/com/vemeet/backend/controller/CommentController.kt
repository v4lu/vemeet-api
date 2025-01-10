package com.vemeet.backend.controller
import com.vemeet.backend.dto.CommentCreateRequest
import com.vemeet.backend.dto.CommentReactionCreateRequest
import com.vemeet.backend.dto.CommentResponse
import com.vemeet.backend.dto.CommentUpdateRequest
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.CommentService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.CognitoIdExtractor
import org.springframework.web.bind.annotation.*
import org.springframework.http.ResponseEntity
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication

@RestController
@RequestMapping("/v1/comments")
@Tag(name = "Comment", description = "Comment endpoints")
class CommentController(
    private val commentService: CommentService,
    private val userService: UserService
) {

    @PostMapping("/posts/{postId}/comments")
    @Operation(summary = "Create a comment on a post")
    @ApiResponse(responseCode = "200", description = "Comment created successfully",
        content = [Content(schema = Schema(implementation = CommentResponse::class))])
    fun createPostComment(
        @PathVariable postId: Long,
        @RequestBody request: CommentCreateRequest,
        authentication: Authentication
    ): ResponseEntity<CommentResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val comment = commentService.createComment(user, postId, null, request)
        return ResponseEntity.ok(comment)
    }

    @PostMapping("/recipes/{recipeId}/comments")
    @Operation(summary = "Create a comment on a recipe")
    @ApiResponse(responseCode = "200", description = "Comment created successfully",
        content = [Content(schema = Schema(implementation = CommentResponse::class))])
    fun createRecipeComment(
        @PathVariable recipeId: Long,
        @RequestBody request: CommentCreateRequest,
        authentication: Authentication
    ): ResponseEntity<CommentResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val comment = commentService.createComment(user, null, recipeId, request)
        return ResponseEntity.ok(comment)
    }

    @GetMapping("/{commentId}")
    @Operation(summary = "Get a comment by ID")
    @ApiResponse(responseCode = "200", description = "Comment retrieved successfully",
        content = [Content(schema = Schema(implementation = CommentResponse::class))])
    fun getComment(@PathVariable commentId: Long): ResponseEntity<CommentResponse> {
        val comment = commentService.getComment(commentId)
        return ResponseEntity.ok(comment)
    }

    @PatchMapping("/{commentId}")
    @Operation(summary = "Update a comment")
    @ApiResponse(responseCode = "200", description = "Comment updated successfully",
        content = [Content(schema = Schema(implementation = CommentResponse::class))])
    fun updateComment(
        @PathVariable commentId: Long,
        @RequestBody request: CommentUpdateRequest,
        authentication: Authentication,
    ): ResponseEntity<CommentResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)

        val updatedComment = commentService.updateComment(commentId, user, request)
        return ResponseEntity.ok(updatedComment)
    }

    @DeleteMapping("/{commentId}")
    @Operation(summary = "Delete a comment")
    @ApiResponse(responseCode = "204", description = "Comment deleted successfully")
    fun deleteComment(
        @PathVariable commentId: Long,
        authentication: Authentication,
    ): ResponseEntity<Unit> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)

        commentService.deleteComment(commentId, user)
        return ResponseEntity.noContent().build()
    }

    @PostMapping("/{commentId}/reactions")
    @Operation(summary = "Add a reaction to a comment")
    @ApiResponse(responseCode = "200", description = "Reaction added successfully",
        content = [Content(schema = Schema(implementation = CommentResponse::class))])
    fun addCommentReaction(
        @PathVariable commentId: Long,
        @RequestBody request: CommentReactionCreateRequest,
       authentication: Authentication
    ): ResponseEntity<CommentResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)

        val updatedComment = commentService.addReaction(commentId, user, request)
        return ResponseEntity.ok(updatedComment)
    }

    @DeleteMapping("/{commentId}/reactions")
    @Operation(summary = "Remove a reaction from a comment")
    @ApiResponse(responseCode = "200", description = "Reaction removed successfully",
        content = [Content(schema = Schema(implementation = CommentResponse::class))])
    fun removeCommentReaction(
        @PathVariable commentId: Long,
        authentication: Authentication,
    ): ResponseEntity<CommentResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val updatedComment = commentService.removeReaction(commentId, user)
        return ResponseEntity.ok(updatedComment)
    }
}
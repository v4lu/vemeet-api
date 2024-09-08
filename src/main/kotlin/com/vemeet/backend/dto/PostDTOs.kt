package com.vemeet.backend.dto

import com.vemeet.backend.model.Post
import com.vemeet.backend.model.Reaction
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Size
import java.time.format.DateTimeFormatter

@Schema(description = "Post Response object")
data class PostResponse(
    @Schema(description = "Post ID", example = "1")
    val id: Long,

    @Schema(description = "User who created the post")
    val user: UserResponse,

    @Schema(description = "Post content", example = "This is my first post!")
    val content: String?,

    @Schema(description = "List of images associated with the post")
    val images: List<ImageResponse>,

    @Schema(description = "List of reactions to the post")
    val reactions: List<ReactionResponse>,

    @Schema(description = "List of comments on this recipe")
    val comments: List<CommentResponse>,

    @Schema(description = "Post creation date", example = "2024-08-27T10:30:00Z")
    val createdAt: String,

    @Schema(description = "Post last update date", example = "2024-08-27T10:30:00Z")
    val updatedAt: String
){
    companion object {
        fun fromPost(post: Post): PostResponse {
            return PostResponse(
                id = post.id,
                user = UserResponse.fromUser(post.user),
                content = post.content,
                images = post.images.map { ImageResponse.fromImage(it.image) },
                reactions = post.reactions.map { ReactionResponse.fromReaction(it) },
                comments = post.comments.map { CommentResponse.fromComment(it) },
                createdAt = DateTimeFormatter.ISO_INSTANT.format(post.createdAt),
                updatedAt = DateTimeFormatter.ISO_INSTANT.format(post.updatedAt)
            )
        }
    }
}

@Schema(description = "Post Creation Request object")
data class PostCreateRequest(
    @field:Size(max = 1000, message = "Content must not exceed 1000 characters")
    @Schema(description = "Post content", example = "This is my first post!")
    val content: String? = null,

    @field:Size(max = 10, message = "Cannot upload more than 10 images")
    @Schema(description = "List of image IDs to associate with the post")
    val imageIds: List<Long>? = null
) {
    fun isValid(): Boolean = !content.isNullOrBlank() || !imageIds.isNullOrEmpty()
}

@Schema(description = "Post Update Request object")
data class PostUpdateRequest(
    @Schema(description = "Post content", example = "Updated content for my first post!")
    @field:Size(max = 1000, message = "Content must not exceed 1000 characters")
    val content: String?,
)

@Schema(description = "Reaction Response object")
data class ReactionResponse(
    @Schema(description = "Reaction ID", example = "1")
    val id: Long,

    @Schema(description = "User who reacted")
    val user: UserResponse,

    @Schema(description = "Type of reaction", example = "LIKE")
    val reactionType: String,

    @Schema(description = "Reaction creation date", example = "2024-08-27T10:30:00Z")
    val createdAt: String
) {
    companion object {
        fun fromReaction(reaction: Reaction): ReactionResponse {
            return ReactionResponse(
                id = reaction.id,
                user = UserResponse.fromUser(reaction.user),
                reactionType = reaction.reactionType,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(reaction.createdAt)
            )
        }
    }
}

@Schema(description = "Reaction Create Request object")
data class ReactionCreateRequest(
    @Schema(description = "Type of reaction", example = "LIKE")
    val reactionType: String
)
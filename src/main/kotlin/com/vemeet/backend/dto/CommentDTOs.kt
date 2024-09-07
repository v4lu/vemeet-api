package com.vemeet.backend.dto

import com.vemeet.backend.model.Comment
import com.vemeet.backend.model.CommentReaction
import io.swagger.v3.oas.annotations.media.Schema
import java.time.format.DateTimeFormatter


@Schema(description = "Comment Response object")
data class CommentResponse(
    @Schema(description = "Comment ID", example = "1")
    val id: Long,

    @Schema(description = "User who created the comment")
    val user: UserResponse,

    @Schema(description = "Comment content", example = "Great post!")
    val content: String,

    @Schema(description = "Parent comment ID, if this is a reply", example = "2")
    val parentId: Long?,

    @Schema(description = "List of replies to this comment")
    val replies: List<CommentResponse>,

    @Schema(description = "List of reactions to this comment")
    val reactions: List<CommentReactionResponse>,

    @Schema(description = "Comment creation date", example = "2024-08-27T10:30:00Z")
    val createdAt: String,

    @Schema(description = "Comment last update date", example = "2024-08-27T10:30:00Z")
    val updatedAt: String
) {
    companion object {
        fun fromComment(comment: Comment): CommentResponse {
            return CommentResponse(
                id = comment.id,
                user = UserResponse.fromUser(comment.user),
                content = comment.content,
                parentId = comment.parent?.id,
                replies = comment.replies.map { fromComment(it) },
                reactions = comment.reactions.map { CommentReactionResponse.fromCommentReaction(it) },
                createdAt = DateTimeFormatter.ISO_INSTANT.format(comment.createdAt),
                updatedAt = DateTimeFormatter.ISO_INSTANT.format(comment.updatedAt)
            )
        }
    }
}

@Schema(description = "Comment Reaction Response object")
data class CommentReactionResponse(
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
        fun fromCommentReaction(reaction: CommentReaction): CommentReactionResponse {
            return CommentReactionResponse(
                id = reaction.id,
                user = UserResponse.fromUser(reaction.user),
                reactionType = reaction.reactionType,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(reaction.createdAt)
            )
        }
    }
}

@Schema(description = "Comment Reaction Create Request object")
data class CommentReactionCreateRequest(
    @Schema(description = "Type of reaction", example = "LIKE", required = true)
    val reactionType: String
)

@Schema(description = "Comment Update Request object")
data class CommentUpdateRequest(
    @Schema(description = "Updated comment content", example = "Updated: Great post!", required = true)
    val content: String
)

@Schema(description = "Comment Create Request object")
data class CommentCreateRequest(
    @Schema(description = "Comment content", example = "Great post!", required = true)
    val content: String,

    @Schema(description = "Parent comment ID, if this is a reply", example = "2")
    val parentId: Long?
)
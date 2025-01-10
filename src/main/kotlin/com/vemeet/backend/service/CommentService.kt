package com.vemeet.backend.service
import com.vemeet.backend.dto.*
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.model.Comment
import com.vemeet.backend.model.CommentReaction
import com.vemeet.backend.model.NotificationTypeEnum
import com.vemeet.backend.model.User
import com.vemeet.backend.repository.CommentReactionRepository
import com.vemeet.backend.repository.CommentRepository
import com.vemeet.backend.repository.PostRepository
import com.vemeet.backend.repository.RecipeRepository
import org.apache.coyote.BadRequestException

@Service
class CommentService(
    private val commentRepository: CommentRepository,
    private val commentReactionRepository: CommentReactionRepository,
    private val postRepository: PostRepository,
    private val recipeRepository: RecipeRepository,
    private val notificationService: NotificationService
) {

    @Transactional
    fun createComment(user: User, postId: Long?, recipeId: Long?, request: CommentCreateRequest): CommentResponse {
        if (postId == null && recipeId == null && request.parentId == null) {
            throw IllegalArgumentException("Either postId, recipeId, or parentId must be provided")
        }

        val comment = Comment(
            user = user,
            content = request.content
        )

        var recipeUser = User()
        when {
            request.parentId != null -> {
                val parentComment = commentRepository.findById(request.parentId)
                    .orElseThrow { ResourceNotFoundException("Parent comment not found") }

                comment.parent = parentComment
                recipeUser = comment.user

                when {
                    postId != null && parentComment.post?.id != postId ->
                        throw IllegalArgumentException("Parent comment does not belong to the specified post")
                    recipeId != null && parentComment.recipe?.id != recipeId ->
                        throw IllegalArgumentException("Parent comment does not belong to the specified recipe")
                }
            }
            postId != null -> {
                val post = postRepository.findById(postId)
                    .orElseThrow { ResourceNotFoundException("Post not found") }
                comment.post = post
                recipeUser = post.user
            }
            recipeId != null -> {
                val recipe = recipeRepository.findById(recipeId)
                    .orElseThrow { ResourceNotFoundException("Recipe not found") }
                comment.recipe = recipe
                recipeUser = recipe.user
            }
        }

        val savedComment = commentRepository.save(comment)
        notificationService.createNotification(
            recipeUser.id,
            NotificationTypeEnum.NEW_COMMENT.typeName,
            "New comment from ${user.username}"
        )
        return CommentResponse.fromComment(savedComment)
    }


    @Transactional(readOnly = true)
    fun getComment(id: Long): CommentResponse {
        val comment = commentRepository.findById(id).orElseThrow { ResourceNotFoundException("Comment not found") }
        return CommentResponse.fromComment(comment)
    }

    @Transactional
    fun updateComment(id: Long, user: User, request: CommentUpdateRequest): CommentResponse {
        val comment = commentRepository.findById(id).orElseThrow { ResourceNotFoundException("Comment not found") }
        if (comment.user.id != user.id) {
            throw NotAllowedException("You don't have permission to update this comment")
        }
        comment.content = request.content
        val updatedComment = commentRepository.save(comment)
        return CommentResponse.fromComment(updatedComment)
    }

    @Transactional
    fun deleteComment(id: Long, user: User) {
        val comment = commentRepository.findById(id).orElseThrow { ResourceNotFoundException("Comment not found") }
        if (comment.user.id != user.id) {
            throw NotAllowedException("You don't have permission to delete this comment")
        }
        commentRepository.delete(comment)
    }

    @Transactional
    fun addReaction(commentId: Long, user: User, request: CommentReactionCreateRequest): CommentResponse {
        val comment = commentRepository.findById(commentId).orElseThrow { ResourceNotFoundException("Comment not found") }
        val existingReaction = commentReactionRepository.findByCommentIdAndUserId(commentId, user.id)

        if (existingReaction != null) {
            if(request.reactionType != "LIKE") {
                throw BadRequestException("Currently we support only likes")
            }

            existingReaction.reactionType = request.reactionType
            commentReactionRepository.save(existingReaction)
        } else {
            val newReaction = CommentReaction(
                user = user,
                comment = comment,
                reactionType = request.reactionType
            )
            commentReactionRepository.save(newReaction)
            comment.reactions.add(newReaction)
        }

        val commentOwner = comment.user
        if (commentOwner.id != user.id) {  // Don't notify if user likes his own post ( crazy but okay )
            notificationService.createNotification(
                commentOwner.id,
                NotificationTypeEnum.NEW_REACTION.typeName,
                "${user.username} reacted to your comment"
            )
        }
        return CommentResponse.fromComment(comment)
    }

    @Transactional
    fun removeReaction(commentId: Long, user: User): CommentResponse {
        val comment = commentRepository.findById(commentId).orElseThrow { ResourceNotFoundException("Comment not found") }
        val reaction = commentReactionRepository.findByCommentIdAndUserId(commentId, user.id)
            ?: throw ResourceNotFoundException("Reaction not found")
        commentReactionRepository.delete(reaction)
        comment.reactions.remove(reaction)
        return CommentResponse.fromComment(comment)
    }

}
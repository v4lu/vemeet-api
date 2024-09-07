package com.vemeet.backend.repository

import com.vemeet.backend.model.CommentReaction
import org.springframework.data.jpa.repository.JpaRepository

interface CommentReactionRepository: JpaRepository<CommentReaction, Long> {
    fun findByCommentIdAndUserId(id: Long, userId: Long): CommentReaction?
}
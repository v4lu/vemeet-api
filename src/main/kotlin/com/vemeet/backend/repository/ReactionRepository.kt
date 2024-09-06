package com.vemeet.backend.repository

import com.vemeet.backend.model.Reaction
import org.springframework.data.jpa.repository.JpaRepository

interface ReactionRepository: JpaRepository<Reaction, Long>  {
    fun findByPostIdAndUserId(postId: Long, userId: Long): Reaction?
    fun deleteByPostIdAndUserId(postId: Long, userId: Long)
    fun countByPostIdAndReactionType(postId: Long, reactionType: String): Long
    fun existsByPostIdAndUserIdAndReactionType(postId: Long, userId: Long, reactionType: String): Boolean
}

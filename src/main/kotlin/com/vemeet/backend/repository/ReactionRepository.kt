package com.vemeet.backend.repository

import com.vemeet.backend.model.ContentType
import com.vemeet.backend.model.Reaction
import org.springframework.data.jpa.repository.JpaRepository

interface ReactionRepository: JpaRepository<Reaction, Long>  {
    fun findByContentTypeAndContentIdAndUserId(contentType: ContentType, contentId: Long, userId: Long): Reaction?
    fun findByContentTypeAndContentId(contentType: ContentType, contentId: Long): List<Reaction>
    fun findByContentTypeAndContentIdIn(contentType: ContentType, contentIds: List<Long>): List<Reaction>
}

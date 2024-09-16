package com.vemeet.backend.service

import com.vemeet.backend.dto.PostResponse
import com.vemeet.backend.repository.PostRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class FeedService(private val postRepository: PostRepository) {

    fun getFeedForUser(userId: Long, pageable: Pageable): Page<PostResponse> {
        return postRepository.findFeedPostsForUser(userId, pageable)
            .map { PostResponse.fromPost(it) }

    }
}
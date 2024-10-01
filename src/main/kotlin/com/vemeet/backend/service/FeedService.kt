package com.vemeet.backend.service

import com.vemeet.backend.cache.UserCache
import com.vemeet.backend.dto.PostResponse
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.repository.PostRepository
import com.vemeet.backend.repository.UserRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service


@Service
class FeedService(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val userCache: UserCache,
) {
    fun getFeedForUser(userId: Long, pageable: Pageable): Page<PostResponse> {
        val posts = postRepository.findFeedPostsForUser(userId, pageable)
        val userIds = posts.content.map { it.userId }.distinct()
        val cachedUsers = userCache.getIDUsers(userIds)
        val missingUserIds = userIds - cachedUsers.keys



        val fetchedUsers = if (missingUserIds.isNotEmpty()) {
            userRepository.findAllById(missingUserIds).associateBy { it.id }.onEach { (id, user) ->
                userCache.cacheIDUser(id, 3600, user)
            }
        } else {
            emptyMap()
        }


        val allUsers = cachedUsers + fetchedUsers

        return posts.map { post ->
            val user = allUsers[post.userId] ?: throw ResourceNotFoundException("User not found for post ${post.id}")
            PostResponse.fromPost(post, user)
        }
    }
}
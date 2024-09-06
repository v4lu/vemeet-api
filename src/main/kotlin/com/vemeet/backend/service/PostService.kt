package com.vemeet.backend.service

import com.vemeet.backend.dto.PostCreateRequest
import com.vemeet.backend.dto.PostResponse
import com.vemeet.backend.dto.PostUpdateRequest
import com.vemeet.backend.dto.ReactionCreateRequest
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.Post
import com.vemeet.backend.model.PostImage
import com.vemeet.backend.model.Reaction
import com.vemeet.backend.model.User
import com.vemeet.backend.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class PostService(
    private val postRepository: PostRepository,
    private val userRepository: UserRepository,
    private val followerRepository: FollowerRepository,
    private val imageRepository: ImageRepository,
    private val reactionRepository: ReactionRepository
) {

    @Transactional(readOnly = true)
    fun getPostById(postId: Long, currentUser: User): PostResponse {
        val post = postRepository.findById(postId)
            .orElseThrow { ResourceNotFoundException("Post not found") }

        val postOwner = userRepository.findUserById(post.user.id)
            ?: throw ResourceNotFoundException("Post owner not found")

        if (postOwner.isPrivate && postOwner.id != currentUser.id && !isFollowing(currentUser, postOwner)) {
            throw NotAllowedException("You don't have permission to view this post")
        }

        return PostResponse.fromPost(post)
    }


    @Transactional(readOnly = true)
    fun getVisiblePosts(currentUser: User, pageable: Pageable): Page<PostResponse> {
        return postRepository.findVisiblePosts(currentUser.id, pageable)
            .map { PostResponse.fromPost(it) }
    }

    @Transactional
    fun createPost(currentUser: User, request: PostCreateRequest): PostResponse {
        val images = imageRepository.findAllById(request.imageIds)
            .filter { it.user?.id == currentUser.id }

        val post = Post(
            user = currentUser,
            content = request.content,
            images = images.mapIndexed { index, image ->
                PostImage(image = image, orderIndex = index)
            }.toMutableList()
        )

        val savedPost = postRepository.save(post)
        return PostResponse.fromPost(savedPost)
    }

    @Transactional
    fun updatePost(postId: Long, currentUser: User, request: PostUpdateRequest): PostResponse {
        val post = postRepository.findById(postId)
            .orElseThrow { ResourceNotFoundException("Post not found") }

        if (post.user.id != currentUser.id) {
            throw NotAllowedException("You don't have permission to update this post")
        }

        request.content?.let { post.content = it }
        request.imageIds?.let {
            val newImages = imageRepository.findAllById(it)
                .filter { image -> image.user?.id == currentUser.id }
            post.images.clear()
            post.images.addAll(newImages.mapIndexed { index, image ->
                PostImage(image = image, orderIndex = index)
            })
        }

        post.updatedAt = Instant.now()
        val updatedPost = postRepository.save(post)
        return PostResponse.fromPost(updatedPost)
    }

    @Transactional
    fun deletePost(postId: Long, currentUser: User) {
        val post = postRepository.findById(postId)
            .orElseThrow { ResourceNotFoundException("Post not found") }

        if (post.user.id != currentUser.id) {
            throw NotAllowedException("You don't have permission to delete this post")
        }

        postRepository.delete(post)
    }

    @Transactional
    fun addReaction(postId: Long, currentUser: User, request: ReactionCreateRequest): PostResponse {
        val post = postRepository.findById(postId)
            .orElseThrow { ResourceNotFoundException("Post not found") }

        val postOwner = userRepository.findUserById(post.user.id)
            ?: throw ResourceNotFoundException("Post owner not found")

        if (postOwner.isPrivate && postOwner.id != currentUser.id && !isFollowing(currentUser, postOwner)) {
            throw NotAllowedException("You don't have permission to react to this post")
        }

        val existingReaction = reactionRepository.findByPostIdAndUserId(postId, currentUser.id)
        if (existingReaction != null) {
            existingReaction.reactionType = request.reactionType
            reactionRepository.save(existingReaction)
        } else {
            val newReaction = Reaction(
                user = currentUser,
                post = post,
                reactionType = request.reactionType
            )
            reactionRepository.save(newReaction)
            post.reactions.add(newReaction)
        }

        return PostResponse.fromPost(post)
    }


    @Transactional
    fun removeReaction(postId: Long, currentUser: User): PostResponse {
        val post = postRepository.findById(postId)
            .orElseThrow { ResourceNotFoundException("Post not found") }

        val reaction = reactionRepository.findByPostIdAndUserId(postId, currentUser.id)
            ?: throw ResourceNotFoundException("Reaction not found")

        reactionRepository.delete(reaction)
        post.reactions.remove(reaction)

        return PostResponse.fromPost(post)
    }

    private fun isFollowing(follower: User, followed: User): Boolean {
        return followerRepository.existsByFollowerIdAndFollowedId(follower.id, followed.id)
    }
}
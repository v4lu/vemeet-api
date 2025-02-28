package com.vemeet.backend.service

import com.vemeet.backend.cache.UserCache
import com.vemeet.backend.dto.FeedItemResponse
import com.vemeet.backend.dto.PostResponse
import com.vemeet.backend.dto.RecipeResponse
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class FeedService(
    private val postRepository: PostRepository,
    private val recipeRepository: RecipeRepository,
    private val userRepository: UserRepository,
    private val reactionRepository: ReactionRepository,
    private val contentTypeRepository: ContentTypeRepository,
    private val userCache: UserCache,
) {
    fun getFeedForUser(userId: Long, pageable: Pageable): Page<FeedItemResponse> {
        val posts = postRepository.findFeedPostsForUser(userId, pageable)
        val recipes = recipeRepository.findFeedRecipesForUser(userId, pageable)

        val combinedContent = (posts.content.map { FeedItem.Post(it) } +
                recipes.content.map { FeedItem.Recipe(it) })
            .sortedByDescending { it.getCreatedAt() }

        val userIds = (posts.content.map { it.user.id } + recipes.content.map { it.user.id }).distinct()
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

        // Fetch reactions for all posts and recipes
        val postContentType = contentTypeRepository.findByName("post")
            ?: throw ResourceNotFoundException("Content type 'post' not found")
        val recipeContentType = contentTypeRepository.findByName("recipe")
            ?: throw ResourceNotFoundException("Content type 'recipe' not found")

        val postIds = posts.content.map { it.id }
        val recipeIds = recipes.content.map { it.id }

        val postReactions = reactionRepository.findByContentTypeAndContentIdIn(postContentType, postIds)
            .groupBy { it.contentId }
        val recipeReactions = reactionRepository.findByContentTypeAndContentIdIn(recipeContentType, recipeIds)
            .groupBy { it.contentId }

        val feedItems = combinedContent.map { item ->
            when (item) {
                is FeedItem.Post -> {
                    val user = allUsers[item.post.user.id] ?: throw ResourceNotFoundException("User not found for post ${item.post.id}")
                    item.post.reactions = postReactions[item.post.id] ?: emptyList()
                    FeedItemResponse.Post(PostResponse.fromPost(item.post, user))
                }
                is FeedItem.Recipe -> {
                    val user = allUsers[item.recipe.user.id] ?: throw ResourceNotFoundException("User not found for recipe ${item.recipe.id}")
                    item.recipe.reactions = recipeReactions[item.recipe.id] ?: emptyList()
                    FeedItemResponse.Recipe(RecipeResponse.fromRecipe(item.recipe, user))
                }
            }
        }

        return PageImpl(feedItems, pageable, posts.totalElements + recipes.totalElements)
    }

    private sealed class FeedItem {
        data class Post(val post: com.vemeet.backend.model.Post) : FeedItem()
        data class Recipe(val recipe: com.vemeet.backend.model.Recipe) : FeedItem()

        fun getCreatedAt(): Instant = when (this) {
            is Post -> post.createdAt
            is Recipe -> recipe.createdAt
        }
    }
}
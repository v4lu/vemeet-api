package com.vemeet.backend.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.*
import com.vemeet.backend.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Service
class RecipeService(
    private val recipeRepository: RecipeRepository,
    private val userService: UserService,
    private val categoryRepository: RecipeCategoryRepository,
    private val tagRepository: TagRepository,
    private val reactionRepository: ReactionRepository,
    private val contentTypeRepository: ContentTypeRepository,
    private val notificationService: NotificationService,
) {

    @Transactional
    fun createRecipe(request: CreateRecipeRequest, accessToken: String): RecipeResponse {
        val user = userService.getSessionUser(accessToken)
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { ResourceNotFoundException("Category not found") }


        val recipe = Recipe(
            user = user,
            title = request.title,
            content = request.content,
            preparationTime = Duration.ofMinutes(request.preparationTime),
            cookingTime = Duration.ofMinutes(request.cookingTime),
            servings = request.servings,
            difficulty = request.difficulty.lowercase(),
            category = category,
        )

        recipe.ingredients = request.ingredients.map { Ingredient(name = it, recipe = recipe) }.toMutableList()

        request.tagIds?.let { tagIds ->
            val tags = tagRepository.findAllById(tagIds).toMutableSet()
            recipe.tags = tags
        }

        request.imageUrls?.let { urls ->
            recipe.images = urls.map { url ->
                RecipeImage(
                    recipe = recipe,
                    imageUrl = url
                )
            }.toMutableList()
        }

        val savedRecipe = recipeRepository.save(recipe)
        return mapToRecipeResponse(savedRecipe, user)
    }

    @Transactional(readOnly = true)
    fun getUserRecipes(userId: Long, pageable: Pageable): Page<RecipeResponse> {
        val user = userService.getUserByIdFull(userId)
        val recipes = recipeRepository.findByUser(user, pageable)

        val contentType = contentTypeRepository.findByName("recipe")
            ?: throw ResourceNotFoundException("Content type 'recipe' not found")

        val recipeIds = recipes.content.map { it.id }
        val allReactions = reactionRepository.findByContentTypeAndContentIdIn(contentType, recipeIds)
        val reactionsByRecipeId = allReactions.groupBy { it.contentId }

        return recipes.map { recipe ->
            recipe.reactions = reactionsByRecipeId[recipe.id] ?: emptyList()
            mapToRecipeResponse(recipe, user)
        }
    }

    @Transactional(readOnly = true)
    fun getRecipe(id: Long): RecipeResponse {
        val recipe = recipeRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Recipe not found") }

        val contentType = contentTypeRepository.findByName("recipe")
            ?: throw ResourceNotFoundException("Content type 'recipe' not found")

        recipe.reactions = reactionRepository.findByContentTypeAndContentId(contentType, id)

        return mapToRecipeResponse(recipe, recipe.user)
    }

    @Transactional(readOnly = true)
    fun getCategories(): List<CategoryResponse> {
        return categoryRepository.findAll().map { CategoryResponse(
            id = it.id,
            name = it.name,
        ) }
    }

    @Transactional
    fun createCategory(request: CategoryRequest): CategoryResponse {
        val category =  categoryRepository.save(RecipeCategory(name = request.name))

        return CategoryResponse(
            id = category.id,
            name = request.name,
        )
    }

    @Transactional(readOnly = true)
    fun getAllRecipes(pageable: Pageable): Page<RecipeResponse> {
        val recipes = recipeRepository.findAll(pageable)
        val contentType = contentTypeRepository.findByName("recipe")
            ?: throw ResourceNotFoundException("Content type 'recipe' not found")

        val recipeIds = recipes.content.map { it.id }
        val allReactions = reactionRepository.findByContentTypeAndContentIdIn(contentType, recipeIds)
        val reactionsByRecipeId = allReactions.groupBy { it.contentId }

        return recipes.map { recipe ->
            recipe.reactions = reactionsByRecipeId[recipe.id] ?: emptyList()
            mapToRecipeResponse(recipe, recipe.user)
        }
    }

    @Transactional
    fun deleteRecipe(id: Long, accessToken: String) {
        val user = userService.getSessionUser(accessToken)
        val recipe = recipeRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Recipe not found") }

        if (recipe.user.id != user.id) {
            throw NotAllowedException("You don't have permission to delete this recipe")
        }

        recipeRepository.delete(recipe)
    }

    @Transactional
    fun removeReaction(recipeId: Long, currentUser: User): RecipeResponse {
        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { ResourceNotFoundException("Recipe not found") }

        val contentType = contentTypeRepository.findByName("recipe")
            ?: throw ResourceNotFoundException("Content type 'recipe' not found")

        val reaction = reactionRepository.findByContentTypeAndContentIdAndUserId(contentType, recipeId, currentUser.id)
            ?: throw ResourceNotFoundException("Reaction not found")

        reactionRepository.delete(reaction)

        recipe.reactions = reactionRepository.findByContentTypeAndContentId(contentType, recipeId)

        return mapToRecipeResponse(recipe, recipe.user)
    }

    @Transactional
    fun addReaction(recipeId: Long, currentUser: User, request: ReactionCreateRequest): RecipeResponse {
        val recipe = recipeRepository.findById(recipeId)
            .orElseThrow { ResourceNotFoundException("Recipe not found") }


        val contentType = contentTypeRepository.findByName("recipe")
            ?: throw ResourceNotFoundException("Content type 'recipe' not found")

        val existingReaction = reactionRepository.findByContentTypeAndContentIdAndUserId(contentType, recipeId, currentUser.id)
        if (existingReaction != null) {
            existingReaction.reactionType = request.reactionType
            reactionRepository.save(existingReaction)
        } else {
            val newReaction = Reaction(
                user = currentUser,
                contentType = contentType,
                contentId = recipeId,
                reactionType = request.reactionType
            )
            reactionRepository.save(newReaction)
        }

        recipe.reactions = reactionRepository.findByContentTypeAndContentId(contentType, recipeId)

        if (recipe.user.id != currentUser.id) {
            notificationService.createNotification(
                recipe.user.id,
                NotificationTypeEnum.NEW_REACTION.typeName,
                "${currentUser.username} liked your recipe: ${recipe.title}"
            )
        }

        return mapToRecipeResponse(recipe, recipe.user)
    }


    @Transactional(readOnly = true)
    fun findAllRecipes(
        title: String?,
        categoryId: Long?,
        tagId: Long?,
        difficulty: String?,
        minServings: Int?,
        maxServings: Int?,
        createdAfter: Instant?,
        createdBefore: Instant?,
        pageable: Pageable,
    ): Page<RecipeResponse> {
        return recipeRepository.findAllWithFilters(
            title, categoryId, tagId, difficulty, minServings, maxServings, createdAfter, createdBefore, pageable
        ).map { mapToRecipeResponse(it, it.user) }
    }

    @Transactional
    fun updateRecipe(id: Long, request: UpdateRecipeRequest, accessToken: String): RecipeResponse {
        val user = userService.getSessionUser(accessToken)
        val recipe = recipeRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Recipe not found") }

        if (recipe.user.id != user.id) {
            throw NotAllowedException("You don't have permission to update this recipe")
        }

        request.title?.let { recipe.title = it }
        request.content?.let { recipe.content = it }
        request.preparationTime?.let { recipe.preparationTime = Duration.ofMinutes(it) }
        request.cookingTime?.let { recipe.cookingTime = Duration.ofMinutes(it) }
        request.servings?.let { recipe.servings = it }
        request.difficulty?.let { recipe.difficulty = it.lowercase() }

        request.categoryId?.let { categoryId ->
            val category = categoryRepository.findById(categoryId)
                .orElseThrow { ResourceNotFoundException("Category not found") }
            recipe.category = category
        }

        request.ingredients?.let { newIngredients ->
            recipe.ingredients.clear()
            recipe.ingredients.addAll(newIngredients.map { Ingredient(name = it, recipe = recipe) })
        }

        request.tagIds?.let { tagIds ->
            val tags = tagRepository.findAllById(tagIds).toMutableSet()
            recipe.tags = tags
        }

        request.imageUrls?.let { urls ->
            recipe.images.clear()
            recipe.images.addAll(urls.map { url -> RecipeImage(recipe = recipe, imageUrl = url) })
        }

        recipe.updatedAt = Instant.now()

        val savedRecipe = recipeRepository.save(recipe)
        return mapToRecipeResponse(savedRecipe, user)
    }


    private fun mapToRecipeResponse(recipe: Recipe, user: User): RecipeResponse {
        return RecipeResponse.fromRecipe(recipe, user)
    }
}
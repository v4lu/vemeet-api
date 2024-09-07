package com.vemeet.backend.service
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import com.fasterxml.jackson.databind.ObjectMapper
import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.Difficulty
import com.vemeet.backend.model.Recipe
import com.vemeet.backend.repository.RecipeCategoryRepository
import com.vemeet.backend.repository.RecipeRepository
import com.vemeet.backend.repository.TagRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable

@Service
class RecipeService(
    private val recipeRepository: RecipeRepository,
    private val userService: UserService,
    private val categoryRepository: RecipeCategoryRepository,
    private val tagRepository: TagRepository,
    private val objectMapper: ObjectMapper
) {

    @Transactional
    fun createRecipe(request: CreateRecipeRequest, accessToken: String): RecipeResponse {
        val user = userService.getSessionUser(accessToken)
        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { ResourceNotFoundException("Category not found") }

        val recipe = Recipe(
            user = user,
            title = request.title,
            content = request.content?.let { objectMapper.readValue(it, Map::class.java) as Map<String, Any> },
            instructions = request.instructions,
            ingredients = request.ingredients,
            preparationTime = Duration.ofMinutes(request.preparationTime),
            cookingTime = Duration.ofMinutes(request.cookingTime),
            servings = request.servings,
            difficulty = request.difficulty,
            category = category
        )

        request.tagIds?.let { tagIds ->
            val tags = tagRepository.findAllById(tagIds).toMutableSet()
            recipe.tags = tags
        }

        val savedRecipe = recipeRepository.save(recipe)
        return mapToRecipeResponse(savedRecipe)
    }

    @Transactional(readOnly = true)
    fun getRecipe(id: Long): RecipeResponse {
        val recipe = recipeRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Recipe not found") }
        return mapToRecipeResponse(recipe)
    }

    @Transactional(readOnly = true)
    fun getAllRecipes(pageable: Pageable): Page<RecipeResponse> {
        return recipeRepository.findAll(pageable).map { mapToRecipeResponse(it) }
    }

    @Transactional
    fun updateRecipe(id: Long, request: CreateRecipeRequest, accessToken: String): RecipeResponse {
        val user = userService.getSessionUser(accessToken)
        val recipe = recipeRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Recipe not found") }

        if (recipe.user.id != user.id) {
            throw NotAllowedException("You don't have permission to update this recipe")
        }

        val category = categoryRepository.findById(request.categoryId)
            .orElseThrow { ResourceNotFoundException("Category not found") }

        recipe.apply {
            title = request.title
            content = request.content?.let { objectMapper.readValue(it, Map::class.java) as Map<String, Any> }
            instructions = request.instructions
            ingredients = request.ingredients
            preparationTime = Duration.ofMinutes(request.preparationTime)
            cookingTime = Duration.ofMinutes(request.cookingTime)
            servings = request.servings
            difficulty = request.difficulty
            this.category = category
            updatedAt = Instant.now()
        }

        request.tagIds?.let { tagIds ->
            val tags = tagRepository.findAllById(tagIds).toMutableSet()
            recipe.tags = tags
        }

        val updatedRecipe = recipeRepository.save(recipe)
        return mapToRecipeResponse(updatedRecipe)
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


    @Transactional(readOnly = true)
    fun findAllRecipes(
        title: String?,
        categoryId: Long?,
        tagId: Long?,
        difficulty: Difficulty?,
        minServings: Int?,
        maxServings: Int?,
        createdAfter: Instant?,
        createdBefore: Instant?,
        pageable: Pageable
    ): Page<RecipeResponse> {
        return recipeRepository.findAllWithFilters(
            title, categoryId, tagId, difficulty, minServings, maxServings, createdAfter, createdBefore, pageable
        ).map { mapToRecipeResponse(it) }
    }


    private fun mapToRecipeResponse(recipe: Recipe): RecipeResponse {
        return RecipeResponse(
            id = recipe.id,
            title = recipe.title,
            content = recipe.content,
            instructions = recipe.instructions,
            ingredients = recipe.ingredients,
            preparationTime = recipe.preparationTime.toMinutes(),
            cookingTime = recipe.cookingTime.toMinutes(),
            servings = recipe.servings,
            difficulty = recipe.difficulty,
            category = CategoryResponse(recipe.category?.id ?: 0, recipe.category?.name ?: ""),
            images = recipe.images.map { RecipeImageResponse(it.id, it.imageUrl) },
            tags = recipe.tags.map { TagResponse(it.id, it.name) },
            createdAt = recipe.createdAt,
            updatedAt = recipe.updatedAt
        )
    }
}
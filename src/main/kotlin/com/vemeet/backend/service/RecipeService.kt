package com.vemeet.backend.service

import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Duration
import java.time.Instant
import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.Ingredient
import com.vemeet.backend.model.Recipe
import com.vemeet.backend.model.RecipeCategory
import com.vemeet.backend.model.RecipeImage
import com.vemeet.backend.repository.RecipeCategoryRepository
import com.vemeet.backend.repository.RecipeImageRepository
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
    private val recipeImageRepository: RecipeImageRepository
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
        return mapToRecipeResponse(savedRecipe)
    }


    @Transactional(readOnly = true)
    fun getRecipe(id: Long): RecipeResponse {
        val recipe = recipeRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Recipe not found") }
        return mapToRecipeResponse(recipe)
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
        return recipeRepository.findAll(pageable).map { mapToRecipeResponse(it) }
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
        difficulty: String?,
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
        return RecipeResponse.fromRecipe(recipe)
    }
}
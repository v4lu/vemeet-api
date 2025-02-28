package com.vemeet.backend.controller

import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.RecipeService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.CognitoIdExtractor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Pageable
import org.springframework.data.domain.Sort
import org.springframework.format.annotation.DateTimeFormat
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*
import java.time.Instant

@RestController
@RequestMapping("/v1/recipes")
@Tag(name = "Recipe", description = "Recipe management APIs")
class RecipeController(
    private val recipeService: RecipeService,
    private val userService: UserService,
) {

    @PostMapping
    @Operation(summary = "Create a new recipe", description = "Creates a new recipe for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Recipe created successfully", content = [Content(schema = Schema(implementation = RecipeResponse::class))])
    fun createRecipe(
        @RequestBody request: CreateRecipeRequest,
        authentication: Authentication,

        ): ResponseEntity<RecipeResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val recipe = recipeService.createRecipe(request, cognitoId)
        return ResponseEntity.ok(recipe)
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a recipe by ID", description = "Retrieves a recipe by its ID")
    @ApiResponse(responseCode = "200", description = "Recipe found", content = [Content(schema = Schema(implementation = RecipeResponse::class))])
    @ApiResponse(responseCode = "404", description = "Recipe not found")
    fun getRecipe(@PathVariable id: Long): ResponseEntity<RecipeResponse> {
        val recipe = recipeService.getRecipe(id)
        return ResponseEntity.ok(recipe)
    }

    @GetMapping
    @Operation(summary = "Get all recipes", description = "Retrieves all recipes with pagination")
    @ApiResponse(responseCode = "200", description = "Recipes found", content = [Content(schema = Schema(implementation = Page::class))])
    fun getAllRecipes(pageable: Pageable): ResponseEntity<Page<RecipeResponse>> {
        val recipes = recipeService.getAllRecipes(pageable)
        return ResponseEntity.ok(recipes)
    }

    @GetMapping("/categories")
    @Operation(summary = "Get all categories")
    @ApiResponse(responseCode = "200", description = "Recipes found", content = [Content(schema = Schema(implementation = CategoryResponse::class))])
    fun getAllCategories(): ResponseEntity<List<CategoryResponse>> {
        val recipes = recipeService.getCategories()
        return ResponseEntity.ok(recipes)
    }

    @PostMapping("/categories")
    @Operation(summary = "Create new category")
    @ApiResponse(responseCode = "200", description = "successfully created new category", content = [Content(schema = Schema(implementation = CategoryResponse::class))])
    fun createCategory(@RequestBody request: CategoryRequest) : ResponseEntity<CategoryResponse> {
        val category = recipeService.createCategory(request)
        return ResponseEntity.ok(category)
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a recipe", description = "Deletes an existing recipe")
    @ApiResponse(responseCode = "204", description = "Recipe deleted successfully")
    @ApiResponse(responseCode = "404", description = "Recipe not found")
    @ApiResponse(responseCode = "403", description = "Not allowed to delete this recipe")
    fun deleteRecipe(@PathVariable id: Long, authentication: Authentication): ResponseEntity<Unit> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        recipeService.deleteRecipe(id, cognitoId)
        return ResponseEntity.noContent().build()
    }


    @GetMapping("/user/{userId}")
    @Operation(summary = "Get user recipes")
    @ApiResponse(responseCode ="200", description = "Successful return", content = [Content(schema = Schema(implementation = Page::class))])
    fun getPostsByUser(
        @PathVariable userId: Long,
        pageable: Pageable
    ) : ResponseEntity<Page<RecipeResponse>> {
        val recipes = recipeService.getUserRecipes(userId, pageable)

        return ResponseEntity.ok(recipes)
    }

    @PostMapping("/{id}/reactions")
    @Operation(summary = "Add a reaction to a recipe", description = "Adds a reaction (like) to the specified recipe")
    @ApiResponse(responseCode = "200", description = "Reaction added successfully", content = [Content(schema = Schema(implementation = RecipeResponse::class))])
    @ApiResponse(responseCode = "404", description = "Recipe not found")
    @ApiResponse(responseCode = "400", description = "Invalid reaction type")
    fun addReaction(
        @PathVariable id: Long,
        @RequestBody request: ReactionCreateRequest,
        authentication: Authentication
    ): ResponseEntity<RecipeResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)
            ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val updatedRecipe = recipeService.addReaction(id, user, request)
        return ResponseEntity.ok(updatedRecipe)
    }

    @DeleteMapping("/{id}/reactions")
    @Operation(summary = "Remove a reaction from a recipe", description = "Removes the user's reaction from the specified recipe")
    @ApiResponse(responseCode = "200", description = "Reaction removed successfully", content = [Content(schema = Schema(implementation = RecipeResponse::class))])
    @ApiResponse(responseCode = "404", description = "Recipe or reaction not found")
    fun removeReaction(
        @PathVariable id: Long,
        authentication: Authentication
    ): ResponseEntity<RecipeResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)
            ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        val updatedRecipe = recipeService.removeReaction(id, user)
        return ResponseEntity.ok(updatedRecipe)
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Update an existing recipe", description = "Updates an existing recipe for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Recipe updated successfully", content = [Content(schema = Schema(implementation = RecipeResponse::class))])
    @ApiResponse(responseCode = "404", description = "Recipe not found")
    @ApiResponse(responseCode = "403", description = "Not allowed to update this recipe")
    fun updateRecipe(
        @PathVariable id: Long,
        @RequestBody request: UpdateRecipeRequest,
        authentication: Authentication
    ): ResponseEntity<RecipeResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication) ?: throw NotAllowedException("Not valid token")
        val updatedRecipe = recipeService.updateRecipe(id, request, cognitoId)
        return ResponseEntity.ok(updatedRecipe)
    }

}
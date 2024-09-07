package com.vemeet.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.vemeet.backend.model.Difficulty
import java.time.Duration
import java.time.Instant

@Schema(description = "Request object for creating a new recipe")
data class CreateRecipeRequest @JsonCreator constructor(
    @Schema(description = "Recipe title", example = "Vegan Chocolate Cake", required = true)
    @JsonProperty("title") val title: String,

    @Schema(description = "Recipe content as a JSON object", example = """{"introduction": "A delicious vegan chocolate cake", "notes": "Best served with vegan ice cream"}""", required = false)
    @JsonProperty("content") val content: String?,

    @Schema(description = "Recipe instructions", example = "1. Mix dry ingredients. 2. Add wet ingredients. 3. Bake for 30 minutes.", required = true)
    @JsonProperty("instructions") val instructions: String,

    @Schema(description = "List of ingredients", example = """["200g flour", "50g cocoa powder", "200ml plant milk"]""", required = true)
    @JsonProperty("ingredients") val ingredients: List<String>,

    @Schema(description = "Preparation time in minutes", example = "15", required = true)
    @JsonProperty("preparationTime") val preparationTime: Long,

    @Schema(description = "Cooking time in minutes", example = "30", required = true)
    @JsonProperty("cookingTime") val cookingTime: Long,

    @Schema(description = "Number of servings", example = "8", required = true)
    @JsonProperty("servings") val servings: Int,

    @Schema(description = "Recipe difficulty", example = "MEDIUM", required = true)
    @JsonProperty("difficulty") val difficulty: Difficulty,

    @Schema(description = "Category ID", example = "1", required = true)
    @JsonProperty("categoryId") val categoryId: Long,

    @Schema(description = "List of tag IDs", example = "[1, 2, 3]", required = false)
    @JsonProperty("tagIds") val tagIds: List<Long>?
)

@Schema(description = "Response object for recipe details")
data class RecipeResponse(
    @Schema(description = "Recipe ID", example = "1")
    val id: Long,

    @Schema(description = "Recipe title", example = "Vegan Chocolate Cake")
    val title: String,

    @Schema(description = "Recipe content as a JSON object")
    val content: Map<String, Any>?,

    @Schema(description = "Recipe instructions")
    val instructions: String,

    @Schema(description = "List of ingredients")
    val ingredients: List<String>,

    @Schema(description = "Preparation time in minutes", example = "15")
    val preparationTime: Long,

    @Schema(description = "Cooking time in minutes", example = "30")
    val cookingTime: Long,

    @Schema(description = "Number of servings", example = "8")
    val servings: Int,

    @Schema(description = "Recipe difficulty", example = "MEDIUM")
    val difficulty: Difficulty,

    @Schema(description = "Recipe category")
    val category: CategoryResponse,

    @Schema(description = "List of recipe images")
    val images: List<RecipeImageResponse>,

    @Schema(description = "List of recipe tags")
    val tags: List<TagResponse>,

    @Schema(description = "Creation date", example = "2024-08-27T10:30:00Z")
    val createdAt: Instant,

    @Schema(description = "Last update date", example = "2024-08-27T10:30:00Z")
    val updatedAt: Instant
)

data class CategoryResponse(
    val id: Long,
    val name: String
)

data class RecipeImageResponse(
    val id: Long,
    val imageUrl: String
)

data class TagResponse(
    val id: Long,
    val name: String
)
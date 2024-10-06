package com.vemeet.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import com.fasterxml.jackson.annotation.JsonCreator
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.databind.JsonNode
import com.vemeet.backend.model.Recipe
import com.vemeet.backend.model.User
import java.time.format.DateTimeFormatter

@Schema(description = "Request object for creating a new recipe")
data class CreateRecipeRequest @JsonCreator constructor(
    @Schema(description = "Recipe title", example = "Vegan Chocolate Cake", required = true)
    @JsonProperty("title") val title: String,

    @Schema(description = "Recipe content as a JSON object", example = """{"introduction": "A delicious vegan chocolate cake", "notes": "Best served with vegan ice cream"}""", required = false)
    @JsonProperty("content") val content: JsonNode?,

    @Schema(description = "List of ingredients", example = """["200g flour", "50g cocoa powder", "200ml plant milk"]""", required = true)
    @JsonProperty("ingredients") val ingredients: List<String>,

    @Schema(description = "Preparation time in minutes", example = "15", required = true)
    @JsonProperty("preparationTime") val preparationTime: Long,

    @Schema(description = "Cooking time in minutes", example = "30", required = true)
    @JsonProperty("cookingTime") val cookingTime: Long,

    @Schema(description = "List of image URLs", example = """["http://example.com/image1.jpg", "http://example.com/image2.jpg"]""", required = false)
    @JsonProperty("imageUrls") val imageUrls: List<String>?,

    @Schema(description = "Number of servings", example = "8", required = true)
    @JsonProperty("servings") val servings: Int,

    @Schema(description = "Recipe difficulty", example = "MEDIUM", required = true)
    @JsonProperty("difficulty") val difficulty: String,

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
    val content: JsonNode?,

    @Schema(description = "List of ingredients")
    val ingredients: List<String>,

    @Schema(description = "Preparation time in minutes", example = "15")
    val preparationTime: Long,

    @Schema(description = "Cooking time in minutes", example = "30")
    val cookingTime: Long,

    @Schema(description = "Number of servings", example = "8")
    val servings: Int,

    @Schema(description = "Recipe difficulty", example = "MEDIUM")
    val difficulty: String,

    @Schema(description = "Recipe category")
    val category: CategoryResponse,

    @Schema(description = "List of recipe images")
    val images: List<RecipeImageResponse>,

    @Schema(description = "List of recipe tags")
    val tags: List<TagResponse>,

    @Schema(description = "List of comments on this recipe")
    val comments: List<CommentResponse>,

    @Schema(description = "List of reactions to the post")
    val reactions: List<ReactionResponse>,

    @Schema(description = "Creation date", example = "2024-08-27T10:30:00Z")
    val createdAt: String,

    @Schema(description = "Last update date", example = "2024-08-27T10:30:00Z")
    val updatedAt: String,

    @Schema(description = "User who created it")
    val user: UserResponse,
) {
    companion object {
        fun fromRecipe(recipe: Recipe, user: User): RecipeResponse {
            return RecipeResponse(
                id = recipe.id,
                title = recipe.title,
                content = recipe.content,
                ingredients = recipe.ingredients.map { it.name },
                preparationTime = recipe.preparationTime.toMinutes(),
                cookingTime = recipe.cookingTime.toMinutes(),
                servings = recipe.servings,
                difficulty = recipe.difficulty,
                category = CategoryResponse(recipe.category?.id ?: 0, recipe.category?.name ?: ""),
                images = recipe.images.map { RecipeImageResponse(it.id, it.imageUrl) },
                tags = recipe.tags.map { TagResponse(it.id, it.name) },
                comments = recipe.comments.map { CommentResponse.fromComment(it) },
                reactions = recipe.reactions.map { ReactionResponse.fromReaction(it) },
                createdAt = DateTimeFormatter.ISO_INSTANT.format(recipe.createdAt),
                updatedAt = DateTimeFormatter.ISO_INSTANT.format(recipe.updatedAt),
                user = UserResponse.fromUser(user)
            )
        }
    }
}


@Schema(description = "Create Category Request")
data class CategoryRequest (
    @Schema(description = "name of category")
    val name: String,
)

@Schema(description = "Response object for category of recipe")
data class CategoryResponse(
    @Schema(description = "Category ID", example = "1")
    val id: Long,
    @Schema(description = "Category name", example = "Breakfast")
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
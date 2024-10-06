package com.vemeet.backend.dto

import io.swagger.v3.oas.annotations.media.Schema

sealed class FeedItemResponse {
    @Schema(oneOf = [PostResponse::class, RecipeResponse::class])
    data class Post(
        @field:Schema(implementation = PostResponse::class)
        val post: PostResponse
    ) : FeedItemResponse()

    @Schema(oneOf = [PostResponse::class, RecipeResponse::class])
    data class Recipe(
        @field:Schema(implementation = RecipeResponse::class)
        val recipe: RecipeResponse
    ) : FeedItemResponse()
}

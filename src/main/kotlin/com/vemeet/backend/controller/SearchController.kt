package com.vemeet.backend.controller

import com.vemeet.backend.dto.RecipeResponse
import com.vemeet.backend.dto.UserResponse
import com.vemeet.backend.dto.VeganLocationResponse
import com.vemeet.backend.service.SearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/search")
@Tag(name = "Search", description = "Search API")
class SearchController(private val searchService: SearchService) {

    @GetMapping("/users")
    @Operation(summary = "Search users by username or name")
    fun searchUsers(
        @Parameter(description = "Search query", required = true)
        @RequestParam search: String
    ): ResponseEntity<List<UserResponse>> {
        val results = searchService.searchUsers(search)
        return ResponseEntity.ok(results)
    }

    @GetMapping("/recipes")
    @Operation(summary = "Search recipes with filters")
    fun searchRecipes(
        @Parameter(description = "Search query")
        @RequestParam(required = false) search: String?,

        @Parameter(description = "Category ID")
        @RequestParam(required = false) categoryId: Long?,

        @Parameter(description = "Tag ID")
        @RequestParam(required = false) tagId: Long?,

        @Parameter(description = "Difficulty level")
        @RequestParam(required = false) difficulty: String?,

        @Parameter(description = "Minimum servings")
        @RequestParam(required = false) minServings: Int?,

        @Parameter(description = "Maximum servings")
        @RequestParam(required = false) maxServings: Int?
    ): ResponseEntity<List<RecipeResponse>> {
        val results = searchService.searchRecipes(
            query = search,
            categoryId = categoryId,
            tagId = tagId,
            difficulty = difficulty,
            minServings = minServings,
            maxServings = maxServings
        )

        return ResponseEntity.ok(results)
    }

    @GetMapping("/locations")
    @Operation(summary = "Search vegan locations with filters")
    fun searchLocations(
        @Parameter(description = "Search query", required = true)
        @RequestParam search: String,

        @Parameter(description = "Location type (e.g., RESTAURANT, CAFE, SHOP)")
        @RequestParam(required = false) type: String?,

        @Parameter(description = "City name")
        @RequestParam(required = false) city: String?,

        @Parameter(description = "Country name")
        @RequestParam(required = false) country: String?,

        @Parameter(description = "Verified locations only")
        @RequestParam(required = false) verified: Boolean?
    ): ResponseEntity<List<VeganLocationResponse>> {
        val results = searchService.searchLocations(
            query = search,
            type = type,
            city = city,
            country = country,
            verified = verified
        )
        return ResponseEntity.ok(results)
    }
}
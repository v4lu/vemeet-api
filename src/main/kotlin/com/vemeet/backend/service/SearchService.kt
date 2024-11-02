package com.vemeet.backend.service

import com.vemeet.backend.dto.RecipeResponse
import com.vemeet.backend.dto.UserResponse
import com.vemeet.backend.dto.VeganLocationResponse
import com.vemeet.backend.repository.RecipeRepository
import com.vemeet.backend.repository.UserRepository
import com.vemeet.backend.repository.VeganLocationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import java.time.Duration


@Service
class SearchService(
    private val userRepository: UserRepository,
    private val recipeRepository: RecipeRepository,
    private val veganLocationRepository: VeganLocationRepository
) {
    fun searchUsers(query: String): List<UserResponse> {
        val users = userRepository.searchByUsernameOrName(query)
        return users.map { UserResponse.fromUser(it) }
    }

    fun searchRecipes(
        query: String?,
        categoryId: Long? = null,
        tagId: Long? = null,
        difficulty: String? = null,
        minServings: Int? = null,
        maxServings: Int? = null
    ): List<RecipeResponse> {
        val recipes = recipeRepository.findAllWithFilters(
            title = query,
            categoryId = categoryId,
            tagId = tagId,
            difficulty = difficulty,
            minServings = minServings,
            maxServings = maxServings
        )

        return recipes.map { recipe -> RecipeResponse.fromRecipe(recipe, recipe.user) }
    }

    fun searchLocations(
        query: String,
        type: String? = null,
        city: String? = null,
        country: String? = null,
        verified: Boolean? = null
    ): List<VeganLocationResponse> {
        val locations = veganLocationRepository.searchLocationsWithFilters(
            query = query,
            type = type,
            city = city,
            country = country,
            verified = verified
        )
        return locations.map { VeganLocationResponse.fromVeganLocation(it) }
    }


    fun searchUsersByLocation(
        latitude: Double,
        longitude: Double,
        maxDistance: Double = 1000.0,
        pageable: Pageable
    ): Page<UserResponse> {
        val users =
            userRepository.findUsersNearLocation(
                latitude = latitude,
                longitude = longitude,
                maxDistance = maxDistance,
                pageable = pageable
            )
        return users.map { UserResponse.fromUser(it) }
    }

    fun searchLocationsNearby(
        latitude: Double,
        longitude: Double,
        maxDistance: Double = 1000.0,
        query: String? = null,
        type: String? = null,
        city: String? = null,
        country: String? = null,
        verified: Boolean? = null,
        pageable: Pageable
    ): Page<VeganLocationResponse> {
        val locations = veganLocationRepository.findLocationsNearby(
            latitude = latitude,
            longitude = longitude,
            maxDistanceKm = maxDistance,
            query = query,
            type = type,
            verified = verified,
            city = city,
            country = country,
            pageable = pageable
        )

        return locations.map { VeganLocationResponse.fromVeganLocation(it) }
    }
}

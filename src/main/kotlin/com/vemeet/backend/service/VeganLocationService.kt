package com.vemeet.backend.service

import com.vemeet.backend.dto.VeganLocationRequest
import com.vemeet.backend.dto.VeganLocationResponse
import com.vemeet.backend.dto.VeganLocationUpdateRequest
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.LocationImage
import com.vemeet.backend.model.VeganLocation
import com.vemeet.backend.repository.VeganLocationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class VeganLocationService(
    private val veganLocationRepository: VeganLocationRepository,
    private val userService: UserService
) {

    @Transactional(readOnly = true)
    fun getLocationById(id: Long): VeganLocation {
        return veganLocationRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Vegan location not found") }
    }

    @Transactional(readOnly = true)
    fun getAllLocations(search: String?, pageable: Pageable): Page<VeganLocation> {
        return if (!search.isNullOrBlank()) {
            veganLocationRepository.findByNameContainingOrDescriptionContainingOrCityContainingAllIgnoreCase(search, search, search, pageable)
        } else {
            veganLocationRepository.findAll(pageable)
        }
    }

    @Transactional
    fun createLocation(request: VeganLocationRequest, accessToken: String): VeganLocationResponse {
        val user = userService.getSessionUser(accessToken)
        val location = VeganLocation(
            name = request.name,
            description = request.description,
            address = request.address,
            city = request.city,
            country = request.country,
            latitude = request.latitude,
            longitude = request.longitude,
            type = request.type,
            websiteUrl = request.websiteUrl,
            phoneNumber = request.phoneNumber,
            openingHours = request.openingHours,
            priceRange = request.priceRange,
            user = user
        )

        request.images?.forEach { imageUrl ->
            location.images.add(LocationImage(location = location, imageUrl = imageUrl))
        }

        val newLoc =  veganLocationRepository.save(location)
        return VeganLocationResponse.fromVeganLocation(newLoc)
    }

    @Transactional
    fun updateLocation(id: Long, request: VeganLocationUpdateRequest, accessToken: String): VeganLocation {
        val user = userService.getSessionUser(accessToken)
        val location = getLocationById(id)

        if (location.user?.id != user.id) {
            throw IllegalArgumentException("You don't have permission to update this location")
        }

        location.apply {
            request.name?.let { name = it }
            request.description?.let { description = it }
            request.address?.let { address = it }
            request.city?.let { city = it }
            request.country?.let { country = it }
            request.latitude?.let { latitude = it }
            request.longitude?.let { longitude = it }
            request.type?.let { type = it }
            request.websiteUrl?.let { websiteUrl = it }
            request.phoneNumber?.let { phoneNumber = it }
            request.openingHours?.let { openingHours = it }
            request.priceRange?.let { priceRange = it }
            updatedAt = Instant.now()
        }

        request.imagesToAdd?.forEach { imageUrl ->
            location.images.add(LocationImage(location = location, imageUrl = imageUrl))
        }

        request.imageIdsToRemove?.let { idsToRemove ->
            location.images.removeIf { it.id in idsToRemove }
        }

        return veganLocationRepository.save(location)
    }

    @Transactional
    fun deleteLocation(id: Long, accessToken: String) {
        val user = userService.getSessionUser(accessToken)
        val location = getLocationById(id)

        if (location.user?.id != user.id) {
            throw IllegalArgumentException("You don't have permission to delete this location")
        }

        veganLocationRepository.delete(location)
    }
}
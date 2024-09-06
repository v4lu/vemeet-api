package com.vemeet.backend.service
import com.vemeet.backend.dto.VeganLocationRequest
import com.vemeet.backend.exception.ResourceNotFoundException
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
    fun getAllLocations(pageable: Pageable): Page<VeganLocation> {
        return veganLocationRepository.findAll(pageable)
    }

    @Transactional
    fun createLocation(request: VeganLocationRequest, accessToken: String): VeganLocation {
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
        return veganLocationRepository.save(location)
    }

    @Transactional
    fun updateLocation(id: Long, request: VeganLocationRequest, accessToken: String): VeganLocation {
        val user = userService.getSessionUser(accessToken)
        val location = getLocationById(id)

        if (location.user?.id != user.id) {
            throw IllegalArgumentException("You don't have permission to update this location")
        }

        location.apply {
            name = request.name
            description = request.description
            address = request.address
            city = request.city
            country = request.country
            latitude = request.latitude
            longitude = request.longitude
            type = request.type
            websiteUrl = request.websiteUrl
            phoneNumber = request.phoneNumber
            openingHours = request.openingHours
            priceRange = request.priceRange
            updatedAt = Instant.now()
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
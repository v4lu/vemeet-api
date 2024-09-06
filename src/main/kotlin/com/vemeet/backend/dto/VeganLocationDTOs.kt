package com.vemeet.backend.dto

import com.vemeet.backend.model.VeganLocation
import io.swagger.v3.oas.annotations.media.Schema
import java.time.format.DateTimeFormatter

@Schema(description = "Vegan Location Response object")
data class VeganLocationResponse(
    @Schema(description = "Location ID", example = "1")
    val id: Long,

    @Schema(description = "Location name", example = "Green Cafe")
    val name: String,

    @Schema(description = "Location description", example = "A cozy vegan cafe with a variety of plant-based options")
    val description: String?,

    @Schema(description = "Full address", example = "123 Green St, Veganville")
    val address: String,

    @Schema(description = "City", example = "Veganville")
    val city: String,

    @Schema(description = "Country", example = "Veganland")
    val country: String,

    @Schema(description = "Latitude", example = "40.7128")
    val latitude: Double,

    @Schema(description = "Longitude", example = "-74.0060")
    val longitude: Double,

    @Schema(description = "Location type", example = "RESTAURANT")
    val type: String,

    @Schema(description = "Website URL", example = "https://www.greencafe.com")
    val websiteUrl: String?,

    @Schema(description = "Phone number", example = "+1234567890")
    val phoneNumber: String?,

    @Schema(description = "Opening hours", example = "Mon-Fri: 9AM-9PM, Sat-Sun: 10AM-8PM")
    val openingHours: String?,

    @Schema(description = "Price range", example = "$$")
    val priceRange: String?,

    @Schema(description = "User who added the location")
    val user: UserResponse?,

    @Schema(description = "Whether the location is verified", example = "true")
    val isVerified: Boolean,

    @Schema(description = "Creation date", example = "2024-08-27T10:30:00Z")
    val createdAt: String,

    @Schema(description = "Last update date", example = "2024-08-27T10:30:00Z")
    val updatedAt: String
) {
    companion object {
        fun fromVeganLocation(location: VeganLocation): VeganLocationResponse {
            return VeganLocationResponse(
                id = location.id,
                name = location.name,
                description = location.description,
                address = location.address,
                city = location.city,
                country = location.country,
                latitude = location.latitude,
                longitude = location.longitude,
                type = location.type,
                websiteUrl = location.websiteUrl,
                phoneNumber = location.phoneNumber,
                openingHours = location.openingHours,
                priceRange = location.priceRange,
                user = location.user?.let { UserResponse.fromUser(it) },
                isVerified = location.isVerified,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(location.createdAt),
                updatedAt = DateTimeFormatter.ISO_INSTANT.format(location.updatedAt)
            )
        }
    }
}

@Schema(description = "Vegan Location Create/Update Request object")
data class VeganLocationRequest(
    @Schema(description = "Location name", example = "Green Cafe")
    val name: String,

    @Schema(description = "Location description", example = "A cozy vegan cafe with a variety of plant-based options")
    val description: String?,

    @Schema(description = "Full address", example = "123 Green St, Veganville")
    val address: String,

    @Schema(description = "City", example = "Veganville")
    val city: String,

    @Schema(description = "Country", example = "Veganland")
    val country: String,

    @Schema(description = "Latitude", example = "40.7128")
    val latitude: Double,

    @Schema(description = "Longitude", example = "-74.0060")
    val longitude: Double,

    @Schema(description = "Location type", example = "RESTAURANT")
    val type: String,

    @Schema(description = "Website URL", example = "https://www.greencafe.com")
    val websiteUrl: String?,

    @Schema(description = "Phone number", example = "+1234567890")
    val phoneNumber: String?,

    @Schema(description = "Opening hours", example = "Mon-Fri: 9AM-9PM, Sat-Sun: 10AM-8PM")
    val openingHours: String?,

    @Schema(description = "Price range", example = "$$")
    val priceRange: String?
)
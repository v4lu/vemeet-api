package com.vemeet.backend.dto

import com.vemeet.backend.model.LocationImage
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
    val updatedAt: String,

    @Schema(description = "List of image URLs",  example = "[https://www.greencafe.com, https://www.greencafe.com]")
    val images: List<LocationImageResponse>
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
                updatedAt = DateTimeFormatter.ISO_INSTANT.format(location.updatedAt),
                images = location.images.map { LocationImageResponse(it) }
            )
        }
    }
}

@Schema(description = "Images" )
data class LocationImageResponse(
    @Schema(description = "Image id", example = "1")
    val id : Long,

    @Schema(description = "Url of image", example = "https://example.com")
    val url : String,
) {
    constructor(image: LocationImage) : this(
        id = image.id,
        url = image.imageUrl
    )
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
    val priceRange: String?,

    @Schema(description = "List of image URLs",  example = "[https://www.greencafe.com, https://www.greencafe.com]")
    val images: List<String>?
)


@Schema(description = "Vegan Location Update Request object")
data class VeganLocationUpdateRequest(
    @Schema(description = "Location name", example = "Green Cafe")
    val name: String? = null,

    @Schema(description = "Location description", example = "A cozy vegan cafe with a variety of plant-based options")
    val description: String? = null,

    @Schema(description = "Full address", example = "123 Green St, Veganville")
    val address: String? = null,

    @Schema(description = "City", example = "Veganville")
    val city: String? = null,

    @Schema(description = "Country", example = "Veganland")
    val country: String? = null,

    @Schema(description = "Latitude", example = "40.7128")
    val latitude: Double? = null,

    @Schema(description = "Longitude", example = "-74.0060")
    val longitude: Double? = null,

    @Schema(description = "Location type", example = "RESTAURANT")
    val type: String? = null,

    @Schema(description = "Website URL", example = "https://www.greencafe.com")
    val websiteUrl: String? = null,

    @Schema(description = "Phone number", example = "+1234567890")
    val phoneNumber: String? = null,

    @Schema(description = "Opening hours", example = "Mon-Fri: 9AM-9PM, Sat-Sun: 10AM-8PM")
    val openingHours: String? = null,

    @Schema(description = "Price range", example = "$$")
    val priceRange: String? = null,

    @Schema(description = "List of image URLs to add", example = "[https://www.greencafe.com, https://www.greencafe.com]")
    val imagesToAdd: List<String>? = null,

    @Schema(description = "List of image IDs to remove", example = "[1,2,3]")
    val imageIdsToRemove: List<Long>? = null
)
package com.vemeet.backend.dto

import com.vemeet.backend.model.LocationImage
import com.vemeet.backend.model.VeganLocation
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.DecimalMax
import jakarta.validation.constraints.DecimalMin
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import org.hibernate.validator.constraints.Length
import org.hibernate.validator.constraints.URL
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
    @field:NotBlank(message = "Name is required")
    @field:Length(min = 2, max = 255, message = "Name must be between 1 and 255 characters")
    @Schema(description = "Location name", example = "Green Cafe")
    val name: String,

    @field:Length(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Location description", example = "A cozy vegan cafe with a variety of plant-based options")
    val description: String?,

    @field:NotBlank(message = "Address is required")
    @Schema(description = "Full address", example = "123 Green St, Veganville")
    val address: String,

    @field:NotBlank(message = "City is required")
    @field:Length(min = 1, max = 100, message = "City must be between 1 and 100 characters")
    @Schema(description = "City", example = "Veganville")
    val city: String,

    @field:NotBlank(message = "Country is required")
    @field:Length(min = 1, max = 100, message = "Country must be between 1 and 100 characters")
    @Schema(description = "Country", example = "Veganland")
    val country: String,

    @field:NotNull(message = "Latitude is required")
    @field:DecimalMin(value = "-90.0", message = "Latitude must be at least -90.0")
    @field:DecimalMax(value = "90.0", message = "Latitude must be at most 90.0")
    @Schema(description = "Latitude", example = "40.7128")
    val latitude: Double,

    @field:NotNull(message = "Longitude is required")
    @field:DecimalMin(value = "-180.0", message = "Longitude must be at least -180.0")
    @field:DecimalMax(value = "180.0", message = "Longitude must be at most 180.0")
    @Schema(description = "Longitude", example = "-74.0060")
    val longitude: Double,

    @field:NotBlank(message = "Type is required")
    @field:Length(min = 1, max = 50, message = "Type must be between 1 and 50 characters")
    @Schema(description = "Location type", example = "RESTAURANT")
    val type: String,

    @field:URL(message = "Invalid website URL")
    @Schema(description = "Website URL", example = "https://www.greencafe.com")
    val websiteUrl: String?,

    @field:Pattern(regexp = "^\\+?[0-9]{10,20}$", message = "Invalid phone number format")
    @Schema(description = "Phone number", example = "+1234567890")
    val phoneNumber: String?,

    @Schema(description = "Opening hours", example = "Mon-Fri: 9AM-9PM, Sat-Sun: 10AM-8PM")
    val openingHours: String?,

    @field:Length(max = 20, message = "Price range must not exceed 20 characters")
    @Schema(description = "Price range", example = "$$")
    val priceRange: String?,

    @field:Size(max = 10, message = "Maximum 10 images allowed")
    @Schema(description = "List of image URLs", example = "[https://www.greencafe.com/image1.jpg, https://www.greencafe.com/image2.jpg]")
    val images: List<@URL(message = "Invalid image URL") String>?
)

@Schema(description = "Vegan Location Update Request object")
data class VeganLocationUpdateRequest(
    @field:Length(min = 2, max = 255, message = "Name must be between 1 and 255 characters")
    @Schema(description = "Location name", example = "Green Cafe")
    val name: String? = null,

    @field:Length(max = 1000, message = "Description must not exceed 1000 characters")
    @Schema(description = "Location description", example = "A cozy vegan cafe with a variety of plant-based options")
    val description: String? = null,

    @Schema(description = "Full address", example = "123 Green St, Veganville")
    val address: String? = null,

    @field:Length(min = 1, max = 100, message = "City must be between 1 and 100 characters")
    @Schema(description = "City", example = "Veganville")
    val city: String? = null,

    @field:Length(min = 1, max = 100, message = "Country must be between 1 and 100 characters")
    @Schema(description = "Country", example = "Veganland")
    val country: String? = null,

    @field:DecimalMin(value = "-90.0", message = "Latitude must be at least -90.0")
    @field:DecimalMax(value = "90.0", message = "Latitude must be at most 90.0")
    @Schema(description = "Latitude", example = "40.7128")
    val latitude: Double? = null,

    @field:DecimalMin(value = "-180.0", message = "Longitude must be at least -180.0")
    @field:DecimalMax(value = "180.0", message = "Longitude must be at most 180.0")
    @Schema(description = "Longitude", example = "-74.0060")
    val longitude: Double? = null,

    @field:Length(min = 1, max = 50, message = "Type must be between 1 and 50 characters")
    @Schema(description = "Location type", example = "RESTAURANT")
    val type: String? = null,

    @field:URL(message = "Invalid website URL")
    @Schema(description = "Website URL", example = "https://www.greencafe.com")
    val websiteUrl: String? = null,

    @field:Pattern(regexp = "^\\+?[0-9]{10,20}$", message = "Invalid phone number format")
    @Schema(description = "Phone number", example = "+1234567890")
    val phoneNumber: String? = null,

    @Schema(description = "Opening hours", example = "Mon-Fri: 9AM-9PM, Sat-Sun: 10AM-8PM")
    val openingHours: String? = null,

    @field:Length(max = 20, message = "Price range must not exceed 20 characters")
    @Schema(description = "Price range", example = "$$")
    val priceRange: String? = null,

    @field:Size(max = 10, message = "Maximum 10 images allowed to add")
    @Schema(description = "List of image URLs to add", example = "[https://www.greencafe.com/image1.jpg, https://www.greencafe.com/image2.jpg]")
    val imagesToAdd: List<@URL(message = "Invalid image URL") String>? = null,

    @field:Size(max = 10, message = "Maximum 10 image IDs allowed to remove")
    @Schema(description = "List of image IDs to remove", example = "[1,2,3]")
    val imageIdsToRemove: List<Long>? = null
)
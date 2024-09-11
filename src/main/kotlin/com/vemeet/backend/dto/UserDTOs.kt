package com.vemeet.backend.dto

import com.vemeet.backend.model.User
import io.swagger.v3.oas.annotations.media.Schema
import org.hibernate.validator.constraints.URL
import java.time.format.DateTimeFormatter

@Schema(description = "User Response object")
data class UserResponse(
    @Schema(description = "User ID", example = "1")
    val id: Long,

    @Schema(description = "User's username", example = "johndoe")
    val username: String,

    @Schema(description = "User's birthday", example = "1990-01-01T00:00:00Z")
    val birthday: String,

    @Schema(description = "User's AWS Cognito ID", example = "12345678-1234-1234-1234-123456789012")
    val awsCognitoId: String,

    @Schema(description = "User's account creation date", example = "2024-08-27T10:30:00Z")
    val createdAt: String,

    @Schema(description = "Whether the user is verified", example = "true")
    val verified: Boolean,

    @Schema(description = "Whether the user's profile is private", example = "false")
    val isPrivate: Boolean,

    @Schema(description = "Whether the user's inbox is locked", example = "false")
    val inboxLocked: Boolean,

    @Schema(description = "User's gender", example = "Male")
    val gender: String?,

    @Schema(description = "User's bio", example = "Software engineer and travel enthusiast")
    val bio: String?,

    @Schema(description = "User's full name", example = "John Doe")
    val name: String?,

    @Schema(description = "User's country name", example = "United States")
    val countryName: String?,

    @Schema(description = "User's country flag", example = "🇺🇸")
    val countryFlag: String?,

    @Schema(description = "User's country ISO code", example = "US")
    val countryIsoCode: String?,

    @Schema(description = "User's country latitude", example = "37.0902")
    val countryLat: Double?,

    @Schema(description = "User's country longitude", example = "-95.7129")
    val countryLng: Double?,

    @Schema(description = "User's city name", example = "New York")
    val cityName: String?,

    @Schema(description = "User's city latitude", example = "40.7128")
    val cityLat: Double?,

    @Schema(description = "User's city longitude", example = "-74.0060")
    val cityLng: Double?,

    @Schema(description = "User's profile image")
    val profileImage: ImageResponse?
) {
    companion object {
        fun fromUser(user: User): UserResponse {
            return UserResponse(
                id = user.id,
                username = user.username,
                birthday = DateTimeFormatter.ISO_INSTANT.format(user.birthday),
                awsCognitoId = user.awsCognitoId,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(user.createdAt),
                verified = user.verified,
                isPrivate = user.isPrivate,
                inboxLocked = user.inboxLocked,
                gender = user.gender,
                bio = user.bio,
                name = user.name,
                countryName = user.countryName,
                countryFlag = user.countryFlag,
                countryIsoCode = user.countryIsoCode,
                countryLat = user.countryLat,
                countryLng = user.countryLng,
                cityName = user.cityName,
                cityLat = user.cityLat,
                cityLng = user.cityLng,
                profileImage = user.profileImage?.let { ImageResponse.fromImage(it) }
            )
        }
    }
}

@Schema(description = "User Update Request object")
data class UserUpdateRequest(
    @Schema(description = "User's username", example = "johndoe")
    val username: String?,

    @Schema(description = "User's bio", example = "Software engineer and travel enthusiast")
    val bio: String?,

    @Schema(description = "User's full name", example = "John Doe")
    val name: String?,

    @Schema(description = "User's gender", example = "Female")
    val gender: String?,

    @Schema(description = "User's country name", example = "United States")
    val countryName: String?,

    @Schema(description = "User's country flag", example = "🇺🇸")
    val countryFlag: String?,

    @Schema(description = "User's country ISO code", example = "US")
    val countryIsoCode: String?,

    @Schema(description = "User's country latitude", example = "37.0902")
    val countryLat: Double?,

    @Schema(description = "User's country longitude", example = "-95.7129")
    val countryLng: Double?,

    @Schema(description = "User's city name", example = "New York")
    val cityName: String?,

    @Schema(description = "User's city latitude", example = "40.7128")
    val cityLat: Double?,

    @Schema(description = "User's city longitude", example = "-74.0060")
    val cityLng: Double?,

    @Schema(description = "Whether the user's profile is private", example = "false")
    val isPrivate: Boolean?,

    @Schema(description = "Whether the user's inbox is locked", example = "false")
    val inboxLocked: Boolean,

    @Schema(description = "User's new profile image URL", example = "https://example.com/new-profile.jpg")
    @field:URL(message = "URL is required")
    val newImageUrl: String? = null,

    @Schema(description = "ID of an existing image to set as profile picture", example = "1")
    val existingImageId: Long? = null
)
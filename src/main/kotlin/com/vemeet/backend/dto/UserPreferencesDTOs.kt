package com.vemeet.backend.dto


import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "User Preference object")
data class UserPreferenceResponse(
    @Schema(description = "User Preference ID", example = "1")
    val id: Long,

    @Schema(description = "User ID", example = "1")
    val userId: Long,

    @Schema(description = "Minimum age preference", example = "18")
    val minAge: Int,

    @Schema(description = "Maximum age preference", example = "50")
    val maxAge: Int,

    @Schema(description = "Preferred gender", example = "Female")
    val preferredGender: String,

    @Schema(description = "Maximum distance preference in kilometers", example = "50")
    val maxDistance: Int
)

@Schema(description = "User Preference Creation/Update Request object")
data class UserPreferenceRequest(
    @Schema(description = "Minimum age preference", example = "18")
    val minAge: Int,

    @Schema(description = "Maximum age preference", example = "50")
    val maxAge: Int,

    @Schema(description = "Preferred gender", example = "Female")
    val preferredGender: String,

    @Schema(description = "Maximum distance preference in kilometers", example = "50")
    val maxDistance: Int
)
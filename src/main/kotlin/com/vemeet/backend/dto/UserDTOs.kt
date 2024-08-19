package com.vemeet.backend.dto

import io.swagger.v3.oas.annotations.media.Schema
import java.time.LocalDate


@Schema(description = "User session response")
data class SessionResponse(
    @Schema(description = "User ID", example = "1")
    val id: Long,

    @Schema(description = "User's username", example = "johndoe")
    var username: String,

    @Schema(description = "User's birthday", example = "1990-01-01")
    var birthday: LocalDate,

    @Schema(description = "User's AWS Cognito ID", example = "12345678-1234-1234-1234-123456789012")
    var awsCognitoId: String,

    @Schema(description = "Whether the user is verified", example = "true")
    val verified: Boolean,

    @Schema(description = "Whether the user's inbox is locked", example = "false")
    var lockedInbox: Boolean,

    @Schema(description = "Whether the user's profile is private", example = "false")
    var isPrivate: Boolean,
)
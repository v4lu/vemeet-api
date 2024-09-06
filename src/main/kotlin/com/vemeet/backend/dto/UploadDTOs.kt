package com.vemeet.backend.dto

import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Response object for file upload")
data class UploadResponse(
    @Schema(description = "URL of the uploaded file", example = "https://my-bucket.s3.us-west-2.amazonaws.com/123e4567-e89b-12d3-a456-426614174000-myfile.jpg")
    val url: String
)
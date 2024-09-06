package com.vemeet.backend.dto

import com.vemeet.backend.model.Image

data class ImageResponse(
    val id: Long,
    val url: String
) {
    companion object {
        fun fromImage(image: Image): ImageResponse {
            return ImageResponse(
                id = image.id,
                url = image.url
            )
        }
    }
}
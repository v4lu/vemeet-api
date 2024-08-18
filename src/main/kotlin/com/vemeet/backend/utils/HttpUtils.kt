package com.vemeet.backend.utils

import com.vemeet.backend.exception.UnauthorizedException

fun extractAccessToken(authHeader: String): String {
    if (!authHeader.startsWith("Bearer ")) {
        throw UnauthorizedException("Invalid authorization header")
    }
    return authHeader.substring(7) // Remove "Bearer " prefix
}
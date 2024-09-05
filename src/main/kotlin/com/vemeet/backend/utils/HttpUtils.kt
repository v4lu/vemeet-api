package com.vemeet.backend.utils

import org.springframework.security.authentication.BadCredentialsException


fun extractAccessToken(authHeader: String): String {
    if (!authHeader.startsWith("Bearer ")) {
        throw BadCredentialsException("Invalid authorization header")
    }
    return authHeader.substring(7) // Remove "Bearer " prefix
}
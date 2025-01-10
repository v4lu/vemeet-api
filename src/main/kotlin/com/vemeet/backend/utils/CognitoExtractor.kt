package com.vemeet.backend.utils

import org.springframework.security.core.Authentication
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.stereotype.Component

@Component
class CognitoIdExtractor {
    companion object {
        fun extractCognitoId(authentication: Authentication): String? {
            return when {
                authentication.principal is Jwt -> {
                    (authentication.principal as Jwt).claims["sub"] as? String
                }
                else -> {
                    authentication.authorities
                        .find { it.authority.startsWith("COGNITO_ID_") }
                        ?.authority
                        ?.removePrefix("COGNITO_ID_")
                }
            }
        }
    }
}

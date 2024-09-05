package com.vemeet.backend.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.vemeet.backend.dto.ExceptionResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.AuthenticationEntryPoint
import org.springframework.stereotype.Component

@Component
class CustomAuthenticationEntryPoint(private val objectMapper: ObjectMapper) : AuthenticationEntryPoint {
    override fun commence(request: HttpServletRequest, response: HttpServletResponse, authException: AuthenticationException) {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.UNAUTHORIZED.value(),
            message = "Unauthorized: Full authentication is required to access this resource."
        )

        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
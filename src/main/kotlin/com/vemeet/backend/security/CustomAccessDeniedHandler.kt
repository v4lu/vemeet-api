
package com.vemeet.backend.security

import com.fasterxml.jackson.databind.ObjectMapper
import com.vemeet.backend.dto.ExceptionResponse
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.web.access.AccessDeniedHandler
import org.springframework.stereotype.Component

@Component
class CustomAccessDeniedHandler(private val objectMapper: ObjectMapper) : AccessDeniedHandler {
    override fun handle(request: HttpServletRequest, response: HttpServletResponse, accessDeniedException: AccessDeniedException) {
        val errorResponse = ExceptionResponse(
            statusCode = HttpStatus.FORBIDDEN.value(),
            message = "Access Denied: You don't have permission to access this resource."
        )

        response.contentType = "application/json"
        response.status = HttpServletResponse.SC_FORBIDDEN
        response.writer.write(objectMapper.writeValueAsString(errorResponse))
    }
}
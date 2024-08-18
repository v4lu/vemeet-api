package com.vemeet.backend.controller

import com.vemeet.backend.dto.SessionResponse
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.extractAccessToken
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/users")
class UserController(private val userService: UserService) {

    @GetMapping("/")
    fun getCurrentUserSession(@RequestHeader("Authorization") authHeader: String): ResponseEntity<SessionResponse> {
        val accessToken = extractAccessToken(authHeader)
        val userSession = userService.getUserSession(accessToken)
        return ResponseEntity.ok(userSession)
    }


}
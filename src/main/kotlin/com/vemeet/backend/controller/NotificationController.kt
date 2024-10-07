package com.vemeet.backend.controller

import com.vemeet.backend.dto.NotificationResponse
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.NotificationService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.CognitoIdExtractor
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val userService: UserService,
) {
    @GetMapping("/unread")
    fun getUnreadNotifications(authentication: Authentication): List<NotificationResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        return notificationService.getUnreadNotifications(user.id)
    }

    @PostMapping("/mark-read/{notificationId}")
    fun markAsRead(@PathVariable notificationId: Long) {
        notificationService.markAsRead(notificationId)
    }


}
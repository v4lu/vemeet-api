package com.vemeet.backend.controller

import com.vemeet.backend.dto.NotificationResponse
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.NotificationService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.CognitoIdExtractor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/notifications")
@Tag(name = "Notifications", description = "Endpoints for managing user notifications")
class NotificationController(
    private val notificationService: NotificationService,
    private val userService: UserService,
) {
    @GetMapping("/unread")
    @Operation(summary = "Get unread notifications", description = "Retrieves all unread notifications for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved unread notifications")
    @ApiResponse(responseCode = "401", description = "Not authorized")
    fun getUnreadNotifications(authentication: Authentication): List<NotificationResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication) ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        return notificationService.getUnreadNotifications(user.id)
    }

    @PostMapping("/mark-read/{notificationId}")
    @Operation(summary = "Mark notification as read", description = "Marks a specific notification as read")
    @ApiResponse(responseCode = "200", description = "Successfully marked notification as read")
    @ApiResponse(responseCode = "404", description = "Notification not found")
    fun markAsRead(@PathVariable notificationId: Long) {
        notificationService.markAsRead(notificationId)
    }

    @GetMapping("/messages")
    @Operation(summary = "Get message notifications", description = "Retrieves all unread message notifications for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved message notifications")
    @ApiResponse(responseCode = "401", description = "Not authorized")
    fun getMessageNotifications(authentication: Authentication): List<NotificationResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication) ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        return notificationService.getUnreadMessageNotifications(user.id)
    }

    @GetMapping("/non-messages")
    @Operation(summary = "Get non-message notifications", description = "Retrieves all unread non-message notifications for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved non-message notifications")
    @ApiResponse(responseCode = "401", description = "Not authorized")
    fun getNonMessageNotifications(authentication: Authentication): List<NotificationResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication) ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        return notificationService.getUnreadNonMessageNotifications(user.id)
    }

    @PostMapping("/mark-all-read")
    @Operation(summary = "Mark all notifications as read", description = "Marks all unread notifications as read for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully marked all notifications as read")
    @ApiResponse(responseCode = "401", description = "Not authorized")
    fun markAllAsRead(authentication: Authentication) {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication) ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        notificationService.markAllAsRead(user.id)
    }

    @PostMapping("/mark-all-non-message-read")
    @Operation(summary = "Mark all non-message notifications as read", description = "Marks all unread non-message notifications as read for the authenticated user")
    @ApiResponse(responseCode = "200", description = "Successfully marked all non-message notifications as read")
    @ApiResponse(responseCode = "401", description = "Not authorized")
    fun markAllNonMessageAsRead(authentication: Authentication) {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication) ?: throw NotAllowedException("Not valid token")
        val user = userService.getSessionUser(cognitoId)
        notificationService.markAllNonMessageAsRead(user.id)
    }
}
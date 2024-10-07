package com.vemeet.backend.dto

import com.vemeet.backend.model.Notification
import com.vemeet.backend.model.NotificationType
import com.vemeet.backend.model.User
import java.time.format.DateTimeFormatter

data class NotificationResponse (
    val id : Long,
    val user: UserResponse,
    val notificationType: NotificationType,
    val content : String,
    val isRead : Boolean,
    val createdAt: String,
) {
    companion object {
        fun from(notification: Notification, user: User): NotificationResponse{
            return NotificationResponse(
                id = notification.id,
                user = UserResponse.fromUser(user),
                notificationType = notification.notificationType,
                content = notification.content,
                isRead = notification.isRead,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(notification.createdAt),
            )
        }
    }
}
package com.vemeet.backend.dto

import com.vemeet.backend.model.Notification
import com.vemeet.backend.model.NotificationType
import com.vemeet.backend.model.User
import io.swagger.v3.oas.annotations.media.Schema
import java.time.format.DateTimeFormatter

@Schema(description = "Response object for a notification")
data class NotificationResponse(
    @Schema(description = "Unique identifier of the notification", example = "1")
    val id: Long,

    @Schema(description = "User associated with the notification")
    val user: UserResponse,

    @Schema(description = "Type of the notification")
    val notificationType: NotificationType,

    @Schema(description = "Content of the notification", example = "You have a new follower")
    val content: String,

    @Schema(description = "Whether the notification has been read", example = "false")
    val isRead: Boolean,

    @Schema(description = "Timestamp when the notification was created", example = "2023-06-15T10:30:00Z")
    val createdAt: String,
) {
    companion object {
        fun from(notification: Notification, user: User): NotificationResponse {
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

package com.vemeet.backend.service

import com.vemeet.backend.dto.NotificationResponse
import com.vemeet.backend.model.Notification
import com.vemeet.backend.repository.NotificationRepository
import com.vemeet.backend.repository.NotificationTypeRepository
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.temporal.ChronoUnit

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val notificationTypeRepository: NotificationTypeRepository,
    private val userService: UserService,
) {
    fun createNotification(userId: Long, typeName: String, content: String) {
        val notificationType = notificationTypeRepository.findByName(typeName)
            ?: throw IllegalArgumentException("Invalid notification type: $typeName")

        val notification = Notification(
            userId = userId,
            notificationType = notificationType,
            content = content
        )
        notificationRepository.save(notification)
    }

    fun getUnreadNotifications(userId: Long): List<NotificationResponse> {
        val notifications =  notificationRepository.findByUserIdAndIsReadFalse(userId)

        return notifications.map {
            val user = userService.getUserByIdFull(it.userId)
            NotificationResponse.from(it, user)
        }
    }

    fun markAsRead(notificationId: Long) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { NoSuchElementException("Notification not found") }
        notificationRepository.save(notification.copy(isRead = true))
    }

    fun cleanupOldNotifications(daysOld: Long) {
        val cutoffDate = Instant.now().minus(daysOld, ChronoUnit.DAYS)
        val deletedCount = notificationRepository.deleteByCreatedAtBefore(cutoffDate)
        println("Deleted $deletedCount old notifications")
    }
}
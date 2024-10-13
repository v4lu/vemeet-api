package com.vemeet.backend.service

import com.vemeet.backend.dto.NotificationResponse
import com.vemeet.backend.model.Notification
import com.vemeet.backend.model.NotificationTypeEnum
import com.vemeet.backend.repository.NotificationRepository
import com.vemeet.backend.repository.NotificationTypeRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service

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
        val notifications = notificationRepository.findByUserIdAndIsReadFalse(userId)
        return mapNotificationsToResponses(notifications, userId)
    }

    fun getUnreadMessageNotifications(userId: Long): List<NotificationResponse> {
        val notifications = notificationRepository.findByUserIdAndIsReadFalseAndNotificationTypeName(userId, NotificationTypeEnum.NEW_MESSAGE.typeName)
        return mapNotificationsToResponses(notifications, userId)
    }

    fun getUnreadNonMessageNotifications(userId: Long): List<NotificationResponse> {
        val notifications = notificationRepository.findByUserIdAndIsReadFalseAndNotificationTypeNameNot(userId, NotificationTypeEnum.NEW_MESSAGE.typeName)
        return mapNotificationsToResponses(notifications, userId)
    }

    fun markAsRead(notificationId: Long) {
        val notification = notificationRepository.findById(notificationId)
            .orElseThrow { NoSuchElementException("Notification not found") }
        notificationRepository.save(notification.copy(isRead = true))
    }


    @Transactional
    fun markAllAsRead(userId: Long) {
        notificationRepository.markAllAsReadForUser(userId)
    }

    @Transactional
    fun markAllNonMessageAsRead(userId: Long) {
        notificationRepository.markAllNonMessageAsReadForUser(userId, NotificationTypeEnum.NEW_MESSAGE.typeName)
    }

    private fun mapNotificationsToResponses(notifications: List<Notification>, userId: Long): List<NotificationResponse> {
        val user = userService.getUserByIdFull(userId)
        return notifications.map { NotificationResponse.from(it, user) }
    }


}
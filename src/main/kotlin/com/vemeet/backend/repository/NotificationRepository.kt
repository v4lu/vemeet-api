package com.vemeet.backend.repository

import com.vemeet.backend.model.Notification
import com.vemeet.backend.model.NotificationType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface NotificationTypeRepository : JpaRepository<NotificationType, Int> {
    fun findByName(name: String): NotificationType?
}

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {
    fun findByUserIdAndIsReadFalse(userId: Long): List<Notification>
    fun deleteByCreatedAtBefore(before: Instant): Int
}

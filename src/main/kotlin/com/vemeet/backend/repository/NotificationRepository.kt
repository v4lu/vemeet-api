package com.vemeet.backend.repository

import com.vemeet.backend.model.Notification
import com.vemeet.backend.model.NotificationType
import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface NotificationTypeRepository : JpaRepository<NotificationType, Int> {
    fun findByName(name: String): NotificationType?
}

@Repository
interface NotificationRepository : JpaRepository<Notification, Long> {
    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false")
    fun findByUserIdAndIsReadFalse(@Param("userId") userId: Long): List<Notification>

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.notificationType.name = :typeName")
    fun findByUserIdAndIsReadFalseAndNotificationTypeName(
        @Param("userId") userId: Long,
        @Param("typeName") typeName: String
    ): List<Notification>

    @Query("SELECT n FROM Notification n WHERE n.userId = :userId AND n.isRead = false AND n.notificationType.name != :typeName")
    fun findByUserIdAndIsReadFalseAndNotificationTypeNameNot(
        @Param("userId") userId: Long,
        @Param("typeName") typeName: String
    ): List<Notification>

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false")
    fun markAllAsReadForUser(@Param("userId") userId: Long)

    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.userId = :userId AND n.isRead = false AND n.notificationType.name != :messageTypeName")
    fun markAllNonMessageAsReadForUser(
        @Param("userId") userId: Long,
        @Param("messageTypeName") messageTypeName: String
    )
}

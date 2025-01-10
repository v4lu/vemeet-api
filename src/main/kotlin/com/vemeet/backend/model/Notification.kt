package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.OffsetDateTime

@Entity
@Table(name = "notification_types")
data class NotificationType(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(unique = true, nullable = false, length = 50)
    val name: String = "",
)

@Entity
@Table(name = "notifications")
data class Notification(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "notification_type_id", nullable = false)
    val notificationType: NotificationType = NotificationType(),

    @Column(nullable = false)
    val content: String = "",

    @Column(name = "is_read")
    val isRead: Boolean = false,

    @Column(name = "created_at")
    val createdAt: OffsetDateTime = OffsetDateTime.now()
)

enum class NotificationTypeEnum(val id: Int, val typeName: String) {
    NEW_FOLLOWER(1, "new_follower"),
    NEW_REACTION(2, "new_reaction"),
    NEW_COMMENT(3, "new_comment"),
    NEW_MESSAGE(4, "new_message"),
    NEW_MATCH(5, "new_match");

}
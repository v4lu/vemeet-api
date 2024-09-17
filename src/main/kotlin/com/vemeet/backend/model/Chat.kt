package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "chats")
data class Chat(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "user1_id", nullable = false)
    val user1: User = User(),

    @ManyToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "user2_id", nullable = false)
    val user2: User = User(),

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "last_message_id")
    var lastMessage: Message? = null,

    @Column(name = "user1_seen_status")
    var user1SeenStatus: Boolean = false,

    @Column(name = "user2_seen_status")
    var user2SeenStatus: Boolean = false,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
)

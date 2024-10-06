package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "reactions")
data class Reaction(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "content_type_id", nullable = false)
    val contentType: ContentType = ContentType(),

    @Column(name = "content_id", nullable = false)
    val contentId: Long = 0,

    @Column(name = "reaction_type", nullable = false)
    var reactionType: String = "",

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)
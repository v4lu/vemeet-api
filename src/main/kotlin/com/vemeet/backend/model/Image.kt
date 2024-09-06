package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.Instant
import java.time.ZonedDateTime

@Entity
@Table(name = "images")
data class Image(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    val user: User? = null,

    @OneToOne(mappedBy = "profileImage", fetch = FetchType.LAZY)
    val profileUser: User? = null,

    @Column(nullable = false)
    val url: String = "",

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)
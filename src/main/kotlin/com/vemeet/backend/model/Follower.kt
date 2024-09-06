package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "followers")
data class Follower(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "follower_id")
    val follower: User = User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "followed_id")
    val followed: User = User(),

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)
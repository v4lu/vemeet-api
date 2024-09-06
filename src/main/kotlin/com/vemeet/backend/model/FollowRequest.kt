package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "follow_requests")
data class FollowRequest (
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id")
    var requester: User = User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "target_id")
    var target: User = User(),

    @Column(name = "created_at")
    var createdAt: Instant = Instant.now()
)
package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.ZonedDateTime

@Entity
@Table(name = "images")
data class Image(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(nullable = false)
    val url: String,

    @Column(name = "created_at")
    val createdAt: ZonedDateTime = ZonedDateTime.now()
) {
    constructor() : this (
        url = "",
        user = User(),
    )
}
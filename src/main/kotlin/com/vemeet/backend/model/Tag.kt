package com.vemeet.backend.model
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "tags")
data class Tag(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, unique = true)
    var name: String = "",

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
)

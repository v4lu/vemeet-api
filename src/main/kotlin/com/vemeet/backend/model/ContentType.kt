package com.vemeet.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "content_types")
data class ContentType(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(unique = true, nullable = false)
    val name: String = ""
)
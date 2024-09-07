package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "recipe_images")
data class RecipeImage(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipe_id", nullable = false)
    var recipe: Recipe = Recipe(),

    @Column(name = "image_url", nullable = false)
    var imageUrl: String = "",

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)

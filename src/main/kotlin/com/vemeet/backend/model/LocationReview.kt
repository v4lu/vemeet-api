package com.vemeet.backend.model


import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "location_reviews")
data class LocationReview(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id", nullable = false)
    var location: VeganLocation? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    var user: User = User(),

    @Column(nullable = false)
    var rating: Int = 0,

    @Column(columnDefinition = "text")
    var comment: String? = null,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now(),

    @OneToMany(mappedBy = "review", cascade = [CascadeType.ALL], orphanRemoval = true)
    var images: MutableList<ReviewImage> = mutableListOf()
)

@Entity
@Table(name = "review_images")
data class ReviewImage(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = false)
    var review: LocationReview? = null,

    @Column(name = "image_url", nullable = false)
    var imageUrl: String = "",

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)

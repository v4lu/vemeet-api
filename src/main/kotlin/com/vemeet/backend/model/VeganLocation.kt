package com.vemeet.backend.model
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "vegan_locations")
data class VeganLocation(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false)
    var name: String = "",

    @Column(columnDefinition = "text")
    var description: String? = null,

    @Column(nullable = false, columnDefinition = "text")
    var address: String = "",

    @Column(nullable = false, length = 100)
    var city: String = "",

    @Column(nullable = false, length = 100)
    var country: String = "",

    @Column(nullable = false)
    var latitude: Double = 0.0,

    @Column(nullable = false)
    var longitude: Double = 0.0,

    @Column(nullable = false, length = 50)
    var type: String = "",

    @Column(name = "website_url")
    var websiteUrl: String? = null,

    @Column(name = "phone_number", length = 20)
    var phoneNumber: String? = null,

    @Column(name = "opening_hours", columnDefinition = "text")
    var openingHours: String? = null,

    @Column(name = "price_range", length = 20)
    var priceRange: String? = null,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    var user: User? = null,

    @Column(name = "is_verified")
    var isVerified: Boolean = false,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
)
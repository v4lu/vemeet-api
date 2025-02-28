package com.vemeet.backend.model

import jakarta.persistence.*
import java.io.Serializable
import java.time.Instant

@Entity
@Table(name = "user_preferences")
data class UserPreference(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User = User(),

    @Column(name = "min_age")
    var minAge: Int = 1,

    @Column(name = "max_age")
    var maxAge: Int = 99,

    @Column(name = "preferred_gender")
    var preferredGender: String = "Any",

    @Column(name = "max_distance")
    var maxDistance: Int = Int.MAX_VALUE,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "swipes")
data class Swipe(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swiper_id", nullable = false)
    val swiper: User = User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "swiped_id", nullable = false)
    val swiped: User = User(),

    @Column(nullable = false)
    val direction: String = "",

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "matches")
data class Match(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user1_id", nullable = false)
    val user1: User = User(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user2_id", nullable = false)
    val user2: User = User(),

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "swiper_user_profiles")
data class SwiperUserProfile(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(nullable = false, name = "user_id")
    val userId: Long = 0,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at")
    var updatedAt: Instant = Instant.now(),

    @Column
    var description: String? = null,

    @Column(name = "main_image_url")
    var mainImageUrl: String? = null,

    @ElementCollection
    @CollectionTable(name = "swiper_user_profile_images", joinColumns = [JoinColumn(name = "profile_id")])
    @Column(name = "image_url")
    var otherImages: MutableList<String> = mutableListOf()
)

@Entity
@Table(name = "potential_matches")
data class PotentialMatch(
    @EmbeddedId
    val id: PotentialMatchId = PotentialMatchId(),

    @Column(name = "distance")
    val distance: Double = 0.0,
)

@Embeddable
data class PotentialMatchId(
    @Column(name = "user_id")
    val userId: Long = 0,

    @Column(name = "potential_match_id")
    val potentialMatchId: Long = 0
) : Serializable
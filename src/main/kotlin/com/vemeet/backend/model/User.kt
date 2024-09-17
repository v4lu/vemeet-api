package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    var username: String = "",

    @Column(nullable = false)
    val birthday: Instant = Instant.now(),

    @Column(name = "aws_cognito_id", unique = true, nullable = false)
    val awsCognitoId: String = "",

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    var verified: Boolean = false,

    @Column(name = "is_private")
    var isPrivate: Boolean = false,

    @Column(name = "inbox_locked")
    var inboxLocked: Boolean = false,

    @Column(name = "swiper_mode")
    var swiperMode: Boolean = false,

    var gender: String? = null,

    var bio: String? = null,

    var name: String? = null,

    @Column(name = "country_name")
    var countryName: String? = null,

    @Column(name = "country_flag")
    var countryFlag: String? = null,

    @Column(name = "country_iso_code")
    var countryIsoCode: String? = null,

    @Column(name = "country_lat")
    var countryLat: Double? = null,

    @Column(name = "country_lng")
    var countryLng: Double? = null,

    @Column(name = "city_name")
    var cityName: String? = null,

    @Column(name = "city_lat")
    var cityLat: Double? = null,

    @Column(name = "city_lng")
    var cityLng: Double? = null,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "profile_image_id")
    var profileImage: Image? = null
)
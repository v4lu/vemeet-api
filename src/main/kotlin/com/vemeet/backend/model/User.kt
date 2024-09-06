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

    var gender: String? = "",

    @Column(name = "birthplace_lat")
    var birthplaceLat: Double? = null,

    @Column(name = "birthplace_lng")
    var birthplaceLng: Double? = null,

    @Column(name = "birthplace_name")
    var birthplaceName: String? = null,

    @Column(name = "residence_lat")
    var residenceLat: Double? = null,

    @Column(name = "residence_lng")
    var residenceLng: Double? = null,

    @Column(name = "residence_name")
    var residenceName: String? = null,

    var bio: String? = null,

    var name: String? = null,

    @OneToOne(fetch = FetchType.EAGER, cascade = [CascadeType.ALL])
    @JoinColumn(name = "profile_image_id")
    var profileImage: Image? = null
)
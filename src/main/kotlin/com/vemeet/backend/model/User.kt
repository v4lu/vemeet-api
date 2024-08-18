package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.LocalDate
import java.time.ZonedDateTime

@Entity
@Table(name = "users")
data class User(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(unique = true, nullable = false)
    val username: String,

    @Column(nullable = false)
    val birthday: LocalDate,

    @Column(name = "aws_cognito_id", unique = true, nullable = false)
    val awsCognitoId: String,

    @Column(name = "created_at")
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "initial_setup")
    val initialSetup: Boolean = false,

    val verified: Boolean = false,

    @Column(name = "is_private")
    val isPrivate: Boolean = false,

    @Column(name = "inbox_locked")
    val inboxLocked: Boolean = false,

    val name: String? = null,

    val gender: Boolean? = null,

    @Column(name = "birthplace_lat")
    val birthplaceLat: Double? = null,

    @Column(name = "birthplace_lng")
    val birthplaceLng: Double? = null,

    @Column(name = "birthplace_name")
    val birthplaceName: String? = null,

    @Column(name = "residence_lat")
    val residenceLat: Double? = null,

    @Column(name = "residence_lng")
    val residenceLng: Double? = null,

    @Column(name = "residence_name")
    val residenceName: String? = null,

    val bio: String? = null,

    @Column(name = "profile_image_id")
    val profileImageId: Long? = null
) {
    constructor() : this(
        username = "",
        birthday = LocalDate.now(),
        awsCognitoId = ""
    )
}
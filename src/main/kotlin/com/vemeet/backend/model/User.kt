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
    var username: String,

    @Column(nullable = false)
    val birthday: LocalDate,

    @Column(name = "aws_cognito_id", unique = true, nullable = false)
    val awsCognitoId: String,

    @Column(name = "created_at")
    val createdAt: ZonedDateTime = ZonedDateTime.now(),

    @Column(name = "initial_setup")
    var initialSetup: Boolean = false,

    var verified: Boolean = false,

    @Column(name = "is_private")
    var isPrivate: Boolean = false,

    @Column(name = "inbox_locked")
    var inboxLocked: Boolean = false,

    val gender: Boolean? = null,

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

    @Column(name = "profile_image_id")
    val profileImageId: Long? = null
) {
    constructor() : this(
        username = "",
        birthday = LocalDate.now(),
        awsCognitoId = ""
    )
}
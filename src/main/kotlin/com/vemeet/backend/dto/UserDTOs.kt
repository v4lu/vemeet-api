package com.vemeet.backend.dto

import java.time.LocalDate

data class SessionResponse(
    val id: Long,
    var username: String,
    var birthday: LocalDate,
    var awsCognitoId: String,
    val verified : Boolean,
    var lockedInbox: Boolean,
    var isPrivate: Boolean,
)
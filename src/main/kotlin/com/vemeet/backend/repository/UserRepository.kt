package com.vemeet.backend.repository

import com.vemeet.backend.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {
    fun findByUsername(username: String): User?
    fun findByAwsCognitoId(id: String): User?
}
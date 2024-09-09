package com.vemeet.backend.repository

import com.vemeet.backend.model.User
import org.springframework.data.jpa.repository.JpaRepository

interface UserRepository: JpaRepository<User, Long> {
    fun findUserByUsername(username: String): User?
    fun findUserByAwsCognitoId(id: String): User?
    fun findUserById(id: Long): User?
    fun existsByUsername(username: String): Boolean
    fun getAllUsers(): List<User>
}
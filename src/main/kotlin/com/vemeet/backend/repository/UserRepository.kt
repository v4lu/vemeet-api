package com.vemeet.backend.repository

import com.vemeet.backend.model.User
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository: JpaRepository<User, Long> {
    fun findUserByUsername(username: String): User?
    fun findUserByAwsCognitoId(id: String): User?
    fun findUserById(id: Long): User?
    fun existsByUsername(username: String): Boolean

    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchByUsernameOrName(query: String): List<User>
}
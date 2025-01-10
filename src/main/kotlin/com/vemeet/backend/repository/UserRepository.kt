package com.vemeet.backend.repository

import com.vemeet.backend.model.User
import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query

interface UserRepository: JpaRepository<User, Long> {
    fun findUserByUsername(username: String): User?
    fun findUserByAwsCognitoId(id: String): User?
    fun findUserById(id: Long): User?
    fun existsByUsername(username: String): Boolean


    @Query("SELECT u FROM User u WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :query, '%')) OR LOWER(u.name) LIKE LOWER(CONCAT('%', :query, '%'))")
    fun searchByUsernameOrName(query: String): List<User>

    @Query("""
        SELECT u.*, calculate_distance(:latitude, :longitude, u.city_lat, u.city_lng) as distance 
        FROM users u 
        WHERE u.city_lat IS NOT NULL 
          AND u.city_lng IS NOT NULL 
          AND calculate_distance(:latitude, :longitude, u.city_lat, u.city_lng) <= :maxDistance
        ORDER BY distance
        """,
        nativeQuery = true,
        countQuery = """
            SELECT count(*) 
            FROM users u 
            WHERE u.city_lat IS NOT NULL 
              AND u.city_lng IS NOT NULL 
              AND calculate_distance(:latitude, :longitude, u.city_lat, u.city_lng) <= :maxDistance
        """
    )
    fun findUsersNearLocation(
        @Param("latitude") latitude: Double,
        @Param("longitude") longitude: Double,
        @Param("maxDistance") maxDistance: Double,
        pageable: Pageable
    ): Page<User>
}
package com.vemeet.backend.repository

import com.vemeet.backend.model.VeganLocation
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface VeganLocationRepository : JpaRepository<VeganLocation, Long> {
    fun findByCity(city: String): List<VeganLocation>
    fun findByType(type: String): List<VeganLocation>
    fun findByUserId(userId: Long): List<VeganLocation>
        fun findByNameContainingOrDescriptionContainingOrCityContainingAllIgnoreCase(
            name: String,
            description: String,
            city: String,
            pageable: Pageable
        ): Page<VeganLocation>
}
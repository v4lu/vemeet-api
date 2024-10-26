package com.vemeet.backend.repository

import com.vemeet.backend.model.VeganLocation
import org.springframework.data.domain.Page
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository

@Repository
interface VeganLocationRepository : JpaRepository<VeganLocation, Long> {
    fun findByNameContainingOrDescriptionContainingOrCityContainingAllIgnoreCase(
            name: String,
            description: String,
            city: String,
            ): List<VeganLocation>


    @Query("""
    SELECT DISTINCT v FROM VeganLocation v
    WHERE (:query IS NULL OR (
        LOWER(cast(v.name as string)) LIKE LOWER(CONCAT('%', cast(:query as string), '%')) OR
        LOWER(cast(COALESCE(v.description, '') as string)) LIKE LOWER(CONCAT('%', cast(:query as string), '%')) OR
        LOWER(cast(v.city as string)) LIKE LOWER(CONCAT('%', cast(:query as string), '%')) OR
        LOWER(cast(v.country as string)) LIKE LOWER(CONCAT('%', cast(:query as string), '%')) OR
        LOWER(cast(v.address as string)) LIKE LOWER(CONCAT('%', cast(:query as string), '%'))
    ))
    AND (:type IS NULL OR v.type = :type)
    AND (:verified IS NULL OR v.isVerified = :verified)
    AND (:city IS NULL OR LOWER(cast(v.city as string)) = LOWER(cast(:city as string)))
    AND (:country IS NULL OR LOWER(cast(v.country as string)) = LOWER(cast(:country as string)))
""")
    fun searchLocationsWithFilters(
        @Param("query") query: String?,
        @Param("type") type: String?,
        @Param("verified") verified: Boolean?,
        @Param("city") city: String?,
        @Param("country") country: String?
    ): List<VeganLocation>
}
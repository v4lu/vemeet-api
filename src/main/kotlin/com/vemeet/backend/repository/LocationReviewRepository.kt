package com.vemeet.backend.repository

import com.vemeet.backend.model.LocationReview
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface LocationReviewRepository : JpaRepository<LocationReview, Long> {
    fun findByLocationId(locationId: Long, pageable: Pageable): Page<LocationReview>
}
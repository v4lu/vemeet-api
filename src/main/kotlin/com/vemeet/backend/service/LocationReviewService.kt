package com.vemeet.backend.service

import com.vemeet.backend.dto.LocationReviewRequest
import com.vemeet.backend.dto.LocationReviewResponse
import com.vemeet.backend.dto.LocationReviewUpdateRequest
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.LocationReview
import com.vemeet.backend.model.ReviewImage
import com.vemeet.backend.model.User
import com.vemeet.backend.repository.LocationReviewRepository
import com.vemeet.backend.repository.VeganLocationRepository
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class LocationReviewService(
    private val locationReviewRepository: LocationReviewRepository,
    private val veganLocationRepository: VeganLocationRepository,
) {

    @Transactional(readOnly = true)
    fun getReviewById(id: Long): LocationReview {
        return locationReviewRepository.findById(id)
            .orElseThrow { ResourceNotFoundException("Review not found") }
    }

    @Transactional(readOnly = true)
    fun getReviewsForLocation(locationId: Long, pageable: Pageable): Page<LocationReviewResponse> {
        return locationReviewRepository.findByLocationId(locationId, pageable)
            .map { LocationReviewResponse.from(it) }
    }

    @Transactional
    fun createReview(locationId: Long, request: LocationReviewRequest, user: User): LocationReviewResponse {
        val location = veganLocationRepository.findById(locationId)
            .orElseThrow { ResourceNotFoundException("Vegan location not found") }

        val review = LocationReview(
            location = location,
            user = user,
            rating = request.rating,
            comment = request.comment
        )

        request.images?.forEach { imageUrl ->
            review.images.add(ReviewImage(review = review, imageUrl = imageUrl))
        }

        val savedReview = locationReviewRepository.save(review)
        return LocationReviewResponse.from(savedReview)
    }

    @Transactional
    fun updateReview(id: Long, request: LocationReviewUpdateRequest, user: User): LocationReviewResponse {
        val review = getReviewById(id)

        if (review.user.id != user.id) {
            throw IllegalArgumentException("You don't have permission to update this review")
        }

        review.apply {
            request.rating?.let { rating = it }
            request.comment?.let { comment = it }
            updatedAt = Instant.now()
        }

        request.imagesToAdd?.forEach { imageUrl ->
            review.images.add(ReviewImage(review = review, imageUrl = imageUrl))
        }

        request.imageIdsToRemove?.let { idsToRemove ->
            review.images.removeIf { it.id in idsToRemove }
        }

        val updatedReview = locationReviewRepository.save(review)
        return LocationReviewResponse.from(updatedReview)
    }

    @Transactional
    fun deleteReview(id: Long, user: User) {
        val review = getReviewById(id)

        if (review.user.id != user.id) {
            throw IllegalArgumentException("You don't have permission to delete this review")
        }

        locationReviewRepository.delete(review)
    }
}
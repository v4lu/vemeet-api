package com.vemeet.backend.controller

import com.vemeet.backend.dto.ExceptionResponse
import com.vemeet.backend.dto.LocationReviewRequest
import com.vemeet.backend.dto.LocationReviewResponse
import com.vemeet.backend.dto.LocationReviewUpdateRequest
import com.vemeet.backend.service.LocationReviewService
import com.vemeet.backend.service.UserService
import com.vemeet.backend.utils.extractAccessToken
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/v1/vegan-locations-reviews/{locationId}")
@Tag(name = "Location Reviews", description = "Endpoints for managing location reviews")
class LocationReviewController(
    private val locationReviewService: LocationReviewService,
    private val userService: UserService,
) {

    @GetMapping
    @Operation(
        summary = "Get reviews for a location",
        description = "Retrieve a paginated list of reviews for a specific location",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully retrieved reviews",
                content = [Content(schema = Schema(implementation = Page::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Location not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun getReviewsForLocation(
        @PathVariable locationId: Long,
        pageable: Pageable
    ): ResponseEntity<Page<LocationReviewResponse>> {
        val reviews = locationReviewService.getReviewsForLocation(locationId, pageable)
        return ResponseEntity.ok(reviews)
    }

    @PostMapping
    @Operation(
        summary = "Create a review",
        description = "Create a new review for a specific location",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully created review",
                content = [Content(schema = Schema(implementation = LocationReviewResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Location not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun createReview(
        @PathVariable locationId: Long,
        @RequestBody request: LocationReviewRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<LocationReviewResponse> {
        val token = extractAccessToken(authHeader)
        val user = userService.getSessionUser(token)
        val review = locationReviewService.createReview(locationId, request, user)
        return ResponseEntity.ok(review)
    }

    @PutMapping("/{reviewId}")
    @Operation(
        summary = "Update a review",
        description = "Update an existing review",
        responses = [
            ApiResponse(
                responseCode = "200",
                description = "Successfully updated review",
                content = [Content(schema = Schema(implementation = LocationReviewResponse::class))]
            ),
            ApiResponse(
                responseCode = "400",
                description = "Invalid request",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "403",
                description = "Not allowed to update this review",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Review not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun updateReview(
        @PathVariable locationId: Long,
        @PathVariable reviewId: Long,
        @RequestBody request: LocationReviewUpdateRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<LocationReviewResponse> {
        val token = extractAccessToken(authHeader)
        val user = userService.getSessionUser(token)
        val updatedReview = locationReviewService.updateReview(reviewId, request, user)
        return ResponseEntity.ok(updatedReview)
    }

    @DeleteMapping("/{reviewId}")
    @Operation(
        summary = "Delete a review",
        description = "Delete an existing review",
        responses = [
            ApiResponse(responseCode = "204", description = "Successfully deleted review"),
            ApiResponse(
                responseCode = "403",
                description = "Not allowed to delete this review",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "404",
                description = "Review not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun deleteReview(
        @PathVariable locationId: Long,
        @PathVariable reviewId: Long,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Unit> {
        val token = extractAccessToken(authHeader)
        val user = userService.getSessionUser(token)
        locationReviewService.deleteReview(reviewId, user)
        return ResponseEntity.noContent().build()
    }
}
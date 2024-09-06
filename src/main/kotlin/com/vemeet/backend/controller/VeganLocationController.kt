package com.vemeet.backend.controller
import com.vemeet.backend.dto.ExceptionResponse
import com.vemeet.backend.dto.VeganLocationRequest
import com.vemeet.backend.dto.VeganLocationResponse
import com.vemeet.backend.service.VeganLocationService
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
@RequestMapping("/v1/vegan-locations")
@Tag(name = "Vegan Locations", description = "Vegan location endpoints")
class VeganLocationController(
    private val veganLocationService: VeganLocationService
) {

    @GetMapping("/{id}")
    @Operation(
        summary = "Get a vegan location by ID",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved location",
                content = [Content(schema = Schema(implementation = VeganLocationResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Location not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun getLocation(@PathVariable id: Long): ResponseEntity<VeganLocationResponse> {
        val location = veganLocationService.getLocationById(id)
        return ResponseEntity.ok(VeganLocationResponse.fromVeganLocation(location))
    }

    @GetMapping
    @Operation(
        summary = "Get all vegan locations",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully retrieved locations",
                content = [Content(schema = Schema(implementation = Page::class))]
            )
        ]
    )
    fun getAllLocations(pageable: Pageable): ResponseEntity<Page<VeganLocationResponse>> {
        val locations = veganLocationService.getAllLocations(pageable)
        return ResponseEntity.ok(locations.map { VeganLocationResponse.fromVeganLocation(it) })
    }

    @PostMapping
    @Operation(
        summary = "Create a new vegan location",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully created location",
                content = [Content(schema = Schema(implementation = VeganLocationResponse::class))]
            ),
            ApiResponse(
                responseCode = "400", description = "Invalid request",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun createLocation(
        @RequestBody request: VeganLocationRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<VeganLocationResponse> {
        val accessToken = extractAccessToken(authHeader)
        val createdLocation = veganLocationService.createLocation(request, accessToken)
        return ResponseEntity.ok(VeganLocationResponse.fromVeganLocation(createdLocation))
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Update a vegan location",
        responses = [
            ApiResponse(
                responseCode = "200", description = "Successfully updated location",
                content = [Content(schema = Schema(implementation = VeganLocationResponse::class))]
            ),
            ApiResponse(
                responseCode = "404", description = "Location not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "403", description = "Not allowed to update this location",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun updateLocation(
        @PathVariable id: Long,
        @RequestBody request: VeganLocationRequest,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<VeganLocationResponse> {
        val accessToken = extractAccessToken(authHeader)
        val updatedLocation = veganLocationService.updateLocation(id, request, accessToken)
        return ResponseEntity.ok(VeganLocationResponse.fromVeganLocation(updatedLocation))
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Delete a vegan location",
        responses = [
            ApiResponse(responseCode = "204", description = "Successfully deleted location"),
            ApiResponse(
                responseCode = "404", description = "Location not found",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            ),
            ApiResponse(
                responseCode = "403", description = "Not allowed to delete this location",
                content = [Content(schema = Schema(implementation = ExceptionResponse::class))]
            )
        ]
    )
    fun deleteLocation(
        @PathVariable id: Long,
        @RequestHeader("Authorization") authHeader: String
    ): ResponseEntity<Unit> {
        val accessToken = extractAccessToken(authHeader)
        veganLocationService.deleteLocation(id, accessToken)
        return ResponseEntity.noContent().build()
    }
}
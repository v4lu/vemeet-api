package com.vemeet.backend.controller
import com.vemeet.backend.dto.ExceptionResponse
import com.vemeet.backend.dto.VeganLocationRequest
import com.vemeet.backend.dto.VeganLocationResponse
import com.vemeet.backend.dto.VeganLocationUpdateRequest
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.service.VeganLocationService
import com.vemeet.backend.utils.CognitoIdExtractor
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.data.domain.Pageable
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
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
            ),
            ApiResponse(
                responseCode = "422", description = "Location missing some fields or they are not valid, it shows errors too",
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
        description = "Retrieve a paginated list of vegan locations. Supports searching and sorting."
    )
    fun getAllLocations(
        @RequestParam search: String?,
        pageable: Pageable
    ): ResponseEntity<List<VeganLocationResponse>> {
        val locations = veganLocationService.getAllLocations(search)
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
        @Valid @RequestBody request: VeganLocationRequest,
        authentication: Authentication,
    ): ResponseEntity<VeganLocationResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")

        val createdLocation = veganLocationService.createLocation(request, cognitoId)
        return ResponseEntity.ok(createdLocation)
    }

    @PatchMapping("/{id}")
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
        @Valid @RequestBody request: VeganLocationUpdateRequest,
        authentication: Authentication,
        ): ResponseEntity<VeganLocationResponse> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")
        val updatedLocation = veganLocationService.updateLocation(id, request, cognitoId)
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
        authentication: Authentication,

        ): ResponseEntity<Unit> {
        val cognitoId = CognitoIdExtractor.extractCognitoId(authentication)  ?: throw NotAllowedException("Not valid token")

        veganLocationService.deleteLocation(id, cognitoId)
        return ResponseEntity.noContent().build()
    }
}
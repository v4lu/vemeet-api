package com.vemeet.backend.controller

import com.vemeet.backend.model.City
import com.vemeet.backend.model.Country
import com.vemeet.backend.service.LocationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/locations")
@Tag(name = "Location", description = "The Location API")
class LocationController(private val locationService: LocationService) {

    @GetMapping("/countries")
    @Operation(summary = "Get all countries", description = "Retrieves a list of all countries")
    fun getAllCountries(): ResponseEntity<List<Country>> {
        val countries = locationService.getAllCountries()
        return ResponseEntity.ok(countries)
    }

    @GetMapping("/cities/{countryIsoCode}")
    @Operation(summary = "Search cities", description = "Search cities by country ISO code and name")
    fun searchCities(
        @Parameter(description = "Country ISO code") @PathVariable countryIsoCode: String,
        @Parameter(description = "Search term for city name") @RequestParam search: String
    ): ResponseEntity<List<City>> {
        val cities = locationService.searchCitiesByCountryAndName(countryIsoCode, search)
        return ResponseEntity.ok(cities)
    }
}
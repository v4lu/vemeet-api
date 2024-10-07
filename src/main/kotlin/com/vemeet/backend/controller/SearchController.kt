package com.vemeet.backend.controller

import com.vemeet.backend.dto.UserResponse
import com.vemeet.backend.service.SearchService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/v1/search")
@Tag(name = "Search", description = "Search API")
class SearchController(private val searchService: SearchService) {

    @GetMapping("/users")
    @Operation(summary = "Search users by username or name")
    fun searchUsers(
        @Parameter(description = "Search query", required = true)
        @RequestParam search: String
    ): ResponseEntity<List<UserResponse>> {
        val results = searchService.searchUsers(search)
        return ResponseEntity.ok(results)
    }
}
package com.vemeet.backend.controller

import com.vemeet.backend.dto.UploadResponse
import com.vemeet.backend.service.S3Service
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.jetbrains.annotations.NotNull
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import org.springframework.web.multipart.MultipartFile

@RestController
@RequestMapping("/v1/files")
@Tag(name = "File Upload", description = "File upload endpoints")
class UploadController(private val s3Service: S3Service) {

    @PostMapping("/single", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Upload a file to S3",
        responses = [
            ApiResponse(responseCode = "200", description = "File uploaded successfully",
                content = [Content(schema = Schema(implementation = UploadResponse::class))]),
            ApiResponse(responseCode = "400", description = "Invalid input")
        ]
    )
    fun uploadFile(
        @RequestParam("file") @NotNull(value = "it can be empty") file: MultipartFile
    ): ResponseEntity<UploadResponse> {
        val response = s3Service.uploadFile(file)
        return ResponseEntity.ok(response)
    }
}
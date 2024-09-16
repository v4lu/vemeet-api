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
import org.springframework.web.reactive.function.BodyInserters
import org.springframework.web.reactive.function.client.WebClient

@RestController
@RequestMapping("/v1/files")
@Tag(name = "File Upload", description = "File upload endpoints")
class UploadController(
    private val s3Service: S3Service,
    private val webClient: WebClient
    ) {

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

    @PostMapping("/image-avif", consumes = [MediaType.MULTIPART_FORM_DATA_VALUE])
    @Operation(
        summary = "Convert image to AVIF and upload to S3",
        responses = [
            ApiResponse(responseCode = "200", description = "Image converted and uploaded successfully",
                content = [Content(schema = Schema(implementation = UploadResponse::class))]),
        ]
    )
    fun uploadAvifImage(
        @RequestParam("file") @NotNull(value = "it can be empty") file: MultipartFile
    ): ResponseEntity<UploadResponse> {
        val conversionResponse = webClient.post()
            .uri("http://localhost:9001/convert")
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(BodyInserters.fromMultipartData("image", file.resource))
            .retrieve()
            .bodyToMono(String::class.java)
            .block()

        // better error handling in future
        return ResponseEntity.ok(UploadResponse(conversionResponse ?: "Conversion failed"))
    }
}
package com.vemeet.backend.service

import com.vemeet.backend.dto.UploadResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import software.amazon.awssdk.core.sync.RequestBody
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.util.UUID

@Service
class S3Service(private val s3Client: S3Client) {

    @Value("\${aws.s3BucketName}")
    lateinit var bucketName: String

    @Value("\${aws.region}")
    lateinit var region: String

    fun uploadFile(file: MultipartFile): UploadResponse {
        if (file.isEmpty) {
            throw IllegalArgumentException("File cannot be empty")
        }

        val fileExtension = file.originalFilename?.substringAfterLast('.', "")
        val contentType = when (fileExtension?.lowercase()) {
            "mp3", "wav", "ogg" -> "audio/${fileExtension.lowercase()}"
            else -> file.contentType ?: "application/octet-stream"
        }

        val fileName = "${UUID.randomUUID()}-${file.originalFilename}"
        val request = PutObjectRequest.builder()
            .bucket(bucketName)
            .key(fileName)
            .contentType(contentType)
            .build()


        return try {
            s3Client.putObject(request, RequestBody.fromInputStream(file.inputStream, file.size))
            val url = "https://$bucketName.s3.$region.amazonaws.com/$fileName"
            UploadResponse(url)
        } catch (e: Exception) {
            println("Error uploading file: ${e.message}")
            e.printStackTrace()
            throw RuntimeException("Failed to upload file", e)
        }
    }



}
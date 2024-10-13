package com.vemeet.backend.service

import com.vemeet.backend.dto.EncryptionResponse
import org.apache.coyote.BadRequestException
import org.springframework.stereotype.Service
import org.springframework.web.reactive.function.client.WebClient
import org.springframework.web.reactive.function.client.awaitBody
import java.util.Base64

@Service
class CryptoService(
    private val webClient: WebClient,
    ) {

    suspend fun encrypt(message: String): EncryptionResponse {
        return try {
            webClient.post()
                .uri("http://encrpytion:9002/v1/crypto/encrypt")
                .bodyValue(mapOf("message" to message))
                .retrieve()
                .awaitBody()
        } catch (e: Exception) {
            throw RuntimeException("Failed to encrypt message: ${e.message}")
        }
    }

    suspend fun decrypt(encryptedMessage: ByteArray, encryptedDataKey: ByteArray): String {
        return try {
            val response = webClient.post()
                .uri("http://encrpytion:9002/v1/crypto/decrypt")
                .bodyValue(mapOf(
                    "encrypted_message" to Base64.getEncoder().encodeToString(encryptedMessage),
                    "encrypted_data_key" to Base64.getEncoder().encodeToString(encryptedDataKey)
                ))
                .retrieve()
                .awaitBody<Map<String, String>>()


            response["decrypted_message"] ?: throw BadRequestException("Failed to decrypt message")
        } catch (e: Exception) {
            throw BadRequestException("Failed to decrypt message: ${e.message}")
        }
    }
}

package com.vemeet.backend.security

import com.amazonaws.services.kms.AWSKMS
import com.amazonaws.services.kms.model.DecryptRequest
import com.amazonaws.services.kms.model.GenerateDataKeyRequest
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import java.nio.ByteBuffer
import java.security.SecureRandom
import javax.crypto.Cipher
import javax.crypto.spec.GCMParameterSpec
import javax.crypto.spec.SecretKeySpec

@Service
class EncryptionService(private val kmsClient: AWSKMS) {
    @Value("\${aws.kms.keyId}")
    private lateinit var kmsKeyId: String

    private val encryptionType = "AES/GCM/NoPadding"

    fun encrypt(data: String): EncryptedData {
        val dataKey = generateDataKey()
        val iv = generateIv()
        val encryptedContent = encryptWithDataKey(data.toByteArray(), dataKey.plaintextKey.array(), iv)
        return EncryptedData(
            encryptedContent = encryptedContent,
            encryptionType = encryptionType,
            encryptedDataKey = dataKey.encryptedKey,
            iv = iv
        )
    }

    fun decrypt(encryptedData: EncryptedData): String {
        val dataKey = decryptDataKey(encryptedData.encryptedDataKey)
        val decryptedBytes = decryptWithDataKey(encryptedData.encryptedContent, dataKey.array(), encryptedData.iv)
        return String(decryptedBytes)
    }

    private fun generateDataKey(): DataKey {
        val request = GenerateDataKeyRequest()
            .withKeyId(kmsKeyId)
            .withKeySpec("AES_256")
        val result = kmsClient.generateDataKey(request)
        return DataKey(
            plaintextKey = result.plaintext,
            encryptedKey = result.ciphertextBlob
        )
    }

    private fun decryptDataKey(encryptedDataKey: ByteBuffer): ByteBuffer {
        val request = DecryptRequest()
            .withKeyId(kmsKeyId)
            .withCiphertextBlob(encryptedDataKey)
        return kmsClient.decrypt(request).plaintext
    }

    private fun generateIv(): ByteArray {
        val iv = ByteArray(12) // 96 bits
        SecureRandom().nextBytes(iv)
        return iv
    }

    private fun encryptWithDataKey(data: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(encryptionType)
        val secretKey = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, gcmSpec)
        return cipher.doFinal(data)
    }

    private fun decryptWithDataKey(encryptedData: ByteArray, key: ByteArray, iv: ByteArray): ByteArray {
        val cipher = Cipher.getInstance(encryptionType)
        val secretKey = SecretKeySpec(key, "AES")
        val gcmSpec = GCMParameterSpec(128, iv)
        cipher.init(Cipher.DECRYPT_MODE, secretKey, gcmSpec)
        return cipher.doFinal(encryptedData)
    }
}

data class EncryptedData(
    val encryptedContent: ByteArray,
    val encryptionType: String,
    val encryptedDataKey: ByteBuffer,
    val iv: ByteArray
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as EncryptedData

        if (!encryptedContent.contentEquals(other.encryptedContent)) return false
        if (encryptionType != other.encryptionType) return false
        if (encryptedDataKey != other.encryptedDataKey) return false
        if (!iv.contentEquals(other.iv)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = encryptedContent.contentHashCode()
        result = 31 * result + encryptionType.hashCode()
        result = 31 * result + encryptedDataKey.hashCode()
        result = 31 * result + iv.contentHashCode()
        return result
    }
}

data class DataKey(
    val plaintextKey: ByteBuffer,
    val encryptedKey: ByteBuffer
)
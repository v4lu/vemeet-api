package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "chat_assets")
data class ChatAsset(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", nullable = false)
    val message: Message = Message(),

    @Column(name = "file_type", nullable = false)
    val fileType: String = "",

    @Column(name = "file_size", nullable = false)
    val fileSize: Long = 0,

    @Column(name = "encrypted_file_path", nullable = false)
    val encryptedFilePath: String = "",

    @Column(name = "encryption_type", nullable = false)
    val encryptionType: String = "",

    @Column(name = "encrypted_data_key")
    val encryptedDataKey: ByteArray? = null,

    @Column(name = "encryption_iv")
    val encryptionIv: ByteArray? = null,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now()
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatAsset

        if (id != other.id) return false
        if (message != other.message) return false
        if (fileType != other.fileType) return false
        if (fileSize != other.fileSize) return false
        if (encryptedFilePath != other.encryptedFilePath) return false
        if (encryptionType != other.encryptionType) return false
        if (encryptedDataKey != null) {
            if (other.encryptedDataKey == null) return false
            if (!encryptedDataKey.contentEquals(other.encryptedDataKey)) return false
        } else if (other.encryptedDataKey != null) return false
        if (encryptionIv != null) {
            if (other.encryptionIv == null) return false
            if (!encryptionIv.contentEquals(other.encryptionIv)) return false
        } else if (other.encryptionIv != null) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + fileType.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + encryptedFilePath.hashCode()
        result = 31 * result + encryptionType.hashCode()
        result = 31 * result + (encryptedDataKey?.contentHashCode() ?: 0)
        result = 31 * result + (encryptionIv?.contentHashCode() ?: 0)
        result = 31 * result + createdAt.hashCode()
        return result
    }
}

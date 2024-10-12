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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat = Chat(),

    @Column(name = "file_type", nullable = false)
    val fileType: String = "",

    @Column(name = "file_size", nullable = false)
    val fileSize: Long = 0,

    @Column(name = "encrypted_file_path")
    val encryptedFilePath: ByteArray? = null,

    @Column(name = "file_path_encrypted_data_key")
    val filePathEncryptedDataKey: ByteArray? = null,

    @Column(name = "file_path_encryption_version")
    val filePathEncryptionVersion: Int? = null,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "duration_seconds")
    val durationSeconds: Int? = null,

    @Column(name = "mime_type")
    val mimeType: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ChatAsset

        if (id != other.id) return false
        if (message != other.message) return false
        if (chat != other.chat) return false
        if (fileType != other.fileType) return false
        if (fileSize != other.fileSize) return false
        if (encryptedFilePath != null) {
            if (other.encryptedFilePath == null) return false
            if (!encryptedFilePath.contentEquals(other.encryptedFilePath)) return false
        } else if (other.encryptedFilePath != null) return false
        if (filePathEncryptedDataKey != null) {
            if (other.filePathEncryptedDataKey == null) return false
            if (!filePathEncryptedDataKey.contentEquals(other.filePathEncryptedDataKey)) return false
        } else if (other.filePathEncryptedDataKey != null) return false
        if (filePathEncryptionVersion != other.filePathEncryptionVersion) return false
        if (createdAt != other.createdAt) return false
        if (durationSeconds != other.durationSeconds) return false
        if (mimeType != other.mimeType) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + message.hashCode()
        result = 31 * result + chat.hashCode()
        result = 31 * result + fileType.hashCode()
        result = 31 * result + fileSize.hashCode()
        result = 31 * result + (encryptedFilePath?.contentHashCode() ?: 0)
        result = 31 * result + (filePathEncryptedDataKey?.contentHashCode() ?: 0)
        result = 31 * result + (filePathEncryptionVersion ?: 0)
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (durationSeconds ?: 0)
        result = 31 * result + (mimeType?.hashCode() ?: 0)
        return result
    }
}
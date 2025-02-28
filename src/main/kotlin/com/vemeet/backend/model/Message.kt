package com.vemeet.backend.model

import com.vemeet.backend.utils.MessageTypeConverter
import jakarta.persistence.*
import java.time.Instant

@Entity
@Table(name = "messages")
data class Message(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    val chat: Chat = Chat(),

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id", nullable = false)
    val sender: User = User(),



    @Column(name = "message_type", nullable = false)
    val messageType: MessageType = MessageType.TEXT,

    @Column(name = "encrypted_content")
    val encryptedContent: ByteArray? = null,

    @Column(name = "encrypted_data_key")
    val encryptedDataKey: ByteArray? = null,

    @Column(name = "encryption_version")
    val encryptionVersion: Int? = null,

    @Column(name = "created_at")
    val createdAt: Instant = Instant.now(),

    @Column(name = "read_at")
    var readAt: Instant? = null,

    @Column(name = "is_one_time")
    val isOneTime: Boolean = false,

    @Column(name = "content_preview")
    val contentPreview: String? = null
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Message

        if (id != other.id) return false
        if (chat != other.chat) return false
        if (sender != other.sender) return false
        if (messageType != other.messageType) return false
        if (encryptedContent != null) {
            if (other.encryptedContent == null) return false
            if (!encryptedContent.contentEquals(other.encryptedContent)) return false
        } else if (other.encryptedContent != null) return false
        if (encryptedDataKey != null) {
            if (other.encryptedDataKey == null) return false
            if (!encryptedDataKey.contentEquals(other.encryptedDataKey)) return false
        } else if (other.encryptedDataKey != null) return false
        if (encryptionVersion != other.encryptionVersion) return false
        if (createdAt != other.createdAt) return false
        if (readAt != other.readAt) return false
        if (isOneTime != other.isOneTime) return false
        if (contentPreview != other.contentPreview) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + chat.hashCode()
        result = 31 * result + sender.hashCode()
        result = 31 * result + messageType.hashCode()
        result = 31 * result + (encryptedContent?.contentHashCode() ?: 0)
        result = 31 * result + (encryptedDataKey?.contentHashCode() ?: 0)
        result = 31 * result + (encryptionVersion ?: 0)
        result = 31 * result + createdAt.hashCode()
        result = 31 * result + (readAt?.hashCode() ?: 0)
        result = 31 * result + isOneTime.hashCode()
        result = 31 * result + (contentPreview?.hashCode() ?: 0)
        return result
    }
}



enum class MessageType {
    TEXT, IMAGE, VIDEO, AUDIO, FILE, URL
}
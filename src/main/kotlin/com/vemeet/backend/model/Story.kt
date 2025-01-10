package com.vemeet.backend.model

import jakarta.persistence.*
import java.time.Instant
import java.time.Duration

@Entity
@Table(name = "profile_stories")
data class ProfileStory(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
)

@Entity
@Table(name = "story_groups")
data class StoryGroup(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_story_id")
    val profileStory: ProfileStory? = null,

    @Column(name = "title")
    val title: String? = null,

    @Column(name = "image_url")
    val imageUrl: String? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "updated_at", nullable = false)
    val updatedAt: Instant = Instant.now()
)

@Entity
@Table(name = "stories")
data class Story(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "user_id", nullable = false)
    val userId: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_group_id")
    val storyGroup: StoryGroup? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now(),

    @Column(name = "expires_at", nullable = false)
    val expiresAt: Instant= Instant.now(),

    @Column(name = "view_count", nullable = false)
    var viewCount: Int = 0
)

@Entity
@Table(name = "story_views")
data class StoryView(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", nullable = false)
    val story: Story = Story(),

    @Column(name = "viewer_id", nullable = false)
    val viewerId: Long = 0,

    @Column(name = "viewed_at", nullable = false)
    val viewedAt: Instant = Instant.now()
)

@Entity
@Table(name = "story_assets")
data class StoryAsset(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "story_id", unique = true, nullable = false)
    val story: Story = Story(),

    @Enumerated(EnumType.STRING)
    @Column(name = "asset_type", nullable = false)
    val assetType: AssetType = AssetType.IMAGE,

    @Column(name = "content_type")
    val contentType: String? = null,

    @Column(name = "duration")
    val duration: Duration? = null,

    @Column(name = "encrypted_file_path")
    val encryptedFilePath: ByteArray? = null,

    @Column(name = "file_path_encrypted_data_key")
    val filePathEncryptedDataKey: ByteArray? = null,

    @Column(name = "file_path_encryption_version")
    val filePathEncryptionVersion: Int? = null,

    @Column(name = "width")
    val width: Int? = null,

    @Column(name = "height")
    val height: Int? = null,

    @Column(name = "created_at", nullable = false)
    val createdAt: Instant = Instant.now()
) {
    enum class AssetType {
        IMAGE, VIDEO
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as StoryAsset

        if (id != other.id) return false
        if (story != other.story) return false
        if (assetType != other.assetType) return false
        if (contentType != other.contentType) return false
        if (duration != other.duration) return false
        if (encryptedFilePath != null) {
            if (other.encryptedFilePath == null) return false
            if (!encryptedFilePath.contentEquals(other.encryptedFilePath)) return false
        } else if (other.encryptedFilePath != null) return false
        if (filePathEncryptedDataKey != null) {
            if (other.filePathEncryptedDataKey == null) return false
            if (!filePathEncryptedDataKey.contentEquals(other.filePathEncryptedDataKey)) return false
        } else if (other.filePathEncryptedDataKey != null) return false
        if (filePathEncryptionVersion != other.filePathEncryptionVersion) return false
        if (width != other.width) return false
        if (height != other.height) return false
        if (createdAt != other.createdAt) return false

        return true
    }

    override fun hashCode(): Int {
        var result = id.hashCode()
        result = 31 * result + story.hashCode()
        result = 31 * result + assetType.hashCode()
        result = 31 * result + (contentType?.hashCode() ?: 0)
        result = 31 * result + (duration?.hashCode() ?: 0)
        result = 31 * result + (encryptedFilePath?.contentHashCode() ?: 0)
        result = 31 * result + (filePathEncryptedDataKey?.contentHashCode() ?: 0)
        result = 31 * result + (filePathEncryptionVersion ?: 0)
        result = 31 * result + (width ?: 0)
        result = 31 * result + (height ?: 0)
        result = 31 * result + createdAt.hashCode()
        return result
    }
}

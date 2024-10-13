package com.vemeet.backend.dto

import com.vemeet.backend.model.*
import io.swagger.v3.oas.annotations.media.Schema

@Schema(description = "Story Group Response object")
data class StoryGroupResponse(
    @Schema(description = "Story Group ID", example = "1")
    val id: Long,

    @Schema(description = "Profile Story ID", example = "1")
    val profileStoryId: Long?,

    @Schema(description = "Title", example = "My Vacation")
    val title: String?,

    @Schema(description = "Image URL", example = "https://example.com/image.jpg")
    val imageUrl: String?,

    @Schema(description = "Creation date", example = "2024-08-27T10:30:00Z")
    val createdAt: String,

    @Schema(description = "Last update date", example = "2024-08-27T10:30:00Z")
    val updatedAt: String
) {
    companion object {
        fun fromStoryGroup(storyGroup: StoryGroup): StoryGroupResponse {
            return StoryGroupResponse(
                id = storyGroup.id,
                profileStoryId = storyGroup.profileStory?.id,
                title = storyGroup.title,
                imageUrl = storyGroup.imageUrl,
                createdAt = storyGroup.createdAt.toString(),
                updatedAt = storyGroup.updatedAt.toString()
            )
        }
    }
}

@Schema(description = "Story Response object")
data class StoryResponse(
    @Schema(description = "Story ID", example = "1")
    val id: Long,

    @Schema(description = "User ID", example = "1")
    val userId: Long,

    @Schema(description = "Story Group ID", example = "1")
    val storyGroupId: Long?,

    @Schema(description = "Creation date", example = "2024-08-27T10:30:00Z")
    val createdAt: String,

    @Schema(description = "Expiration date", example = "2024-08-28T10:30:00Z")
    val expiresAt: String,

    @Schema(description = "View count", example = "100")
    val viewCount: Int,

    @Schema(description = "Story asset")
    val asset: StoryAssetResponse?
) {
    companion object {
        fun fromStory(story: Story, asset: StoryAsset?, url: String): StoryResponse {
            return StoryResponse(
                id = story.id,
                userId = story.userId,
                storyGroupId = story.storyGroup?.id,
                createdAt = story.createdAt.toString(),
                expiresAt = story.expiresAt.toString(),
                viewCount = story.viewCount,
                asset = asset?.let { StoryAssetResponse.fromStoryAsset(it, url) }
            )
        }
    }
}

@Schema(description = "Story Asset Response object")
data class StoryAssetResponse(
    @Schema(description = "Story Asset ID", example = "1")
    val id: Long,

    @Schema(description = "Asset type", example = "IMAGE")
    val assetType: StoryAsset.AssetType,

    @Schema(description = "Content type", example = "image/jpeg")
    val contentType: String?,

    @Schema(description = "Duration (for videos)", example = "PT30S")
    val duration: String?,

    @Schema(description = "Width", example = "1080")
    val width: Int?,

    @Schema(description = "Height", example = "1920")
    val height: Int?,

    @Schema(description = "Creation date", example = "2024-08-27T10:30:00Z")
    val createdAt: String,

    @Schema(description = "Url", example = "https://example.com/image.jpg")
    val url: String,
) {
    companion object {
        fun fromStoryAsset(asset: StoryAsset, url: String): StoryAssetResponse {
            return StoryAssetResponse(
                id = asset.id,
                assetType = asset.assetType,
                contentType = asset.contentType,
                duration = asset.duration?.toString(),
                width = asset.width,
                height = asset.height,
                createdAt = asset.createdAt.toString(),
                url = url,
            )
        }
    }
}

@Schema(description = "Story View Response object")
data class StoryViewResponse(
    @Schema(description = "Story View ID", example = "1")
    val id: Long,

    @Schema(description = "Story ID", example = "1")
    val storyId: Long,

    @Schema(description = "Viewer ID", example = "1")
    val viewerId: Long,

    @Schema(description = "Viewed at", example = "2024-08-27T10:30:00Z")
    val viewedAt: String
) {
    companion object {
        fun fromStoryView(storyView: StoryView): StoryViewResponse {
            return StoryViewResponse(
                id = storyView.id,
                storyId = storyView.story.id,
                viewerId = storyView.viewerId,
                viewedAt = storyView.viewedAt.toString()
            )
        }
    }
}

@Schema(description = "Create Story Request object")
data class CreateStoryRequest(
    @Schema(description = "Story Group ID", example = "1")
    val storyGroupId: Long?,

    @Schema(description = "Asset type", example = "IMAGE")
    val assetType: StoryAsset.AssetType,

    @Schema(description = "Content type", example = "image/jpeg")
    val contentType: String,

    @Schema(description = "Duration (for videos)", example = "PT30S")
    val duration: String?,

    @Schema(description = "Width", example = "1080")
    val width: Int?,

    @Schema(description = "Height", example = "1920")
    val height: Int?,

    @Schema(description = "url", example = "https://email.com")
    val fileContent: String
)

@Schema(description = "Create Story Group Request object")
data class CreateStoryGroupRequest(
    @Schema(description = "Profile Story ID", example = "1")
    val profileStoryId: Long?,

    @Schema(description = "Title", example = "My Vacation")
    val title: String?,

    @Schema(description = "Image URL", example = "https://example.com/image.jpg")
    val imageUrl: String?
)

@Schema(description = "User Stories Response object")
data class UserStoriesResponse(
    @Schema(description = "User information")
    val user: UserResponse,

    @Schema(description = "List of user's stories")
    val userStories: List<StoryResponse>
) {
    companion object {
        fun fromUserAndStories(user: User, stories: List<Story>, assets: Map<Long, StoryAsset>, urls: Map<Long, String>): UserStoriesResponse {
            return UserStoriesResponse(
                user = UserResponse.fromUser(user),
                userStories = stories.map { story ->
                    val asset = assets[story.id]
                    val url = urls[story.id] ?: ""
                    StoryResponse.fromStory(story, asset, url)
                }
            )
        }
    }
}

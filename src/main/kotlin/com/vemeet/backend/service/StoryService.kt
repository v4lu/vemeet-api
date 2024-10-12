package com.vemeet.backend.service

import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.NotAllowedException
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.*
import com.vemeet.backend.repository.*
import jakarta.transaction.Transactional
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.springframework.stereotype.Service
import java.time.Instant


@Service
class StoryService(
    private val storyRepository: StoryRepository,
    private val storyAssetRepository: StoryAssetRepository,
    private val storyViewRepository: StoryViewRepository,
    private val storyGroupRepository: StoryGroupRepository,
    private val profileStoryRepository: ProfileStoryRepository,
    private val cryptoService: CryptoService
)  {

    @Transactional
    suspend fun createStory(user: User, request: CreateStoryRequest): StoryResponse {
        val storyGroup = request.storyGroupId?.let { storyGroupRepository.findById(it).orElse(null) }

        val story = Story(
            userId = user.id,
            storyGroup = storyGroup,
            expiresAt = Instant.now().plusSeconds(24 * 60 * 60) // 24 hours from now
        )

        val savedStory = withContext(Dispatchers.IO) {
            storyRepository.save(story)
        }

        val encryptionResponse = cryptoService.encrypt(request.fileContent)

        val asset = StoryAsset(
            story = savedStory,
            assetType = request.assetType,
            contentType = request.contentType,
            duration = request.duration?.let { java.time.Duration.parse(it) },
            width = request.width,
            height = request.height,
            encryptedFilePath = encryptionResponse.encryptedMessage.toByteArray(),
            filePathEncryptedDataKey = encryptionResponse.encryptedDataKey.toByteArray(),
            filePathEncryptionVersion = encryptionResponse.keyVersion,
        )
        val savedAsset = withContext(Dispatchers.IO) {
            storyAssetRepository.save(asset)
        }

        return StoryResponse.fromStory(savedStory, savedAsset)
    }


    fun getUserStories(userId: Long): List<StoryResponse> {
        val stories = storyRepository.findByUserIdAndExpiresAtAfter(userId, Instant.now())
        return stories.map { story ->
            val asset = storyAssetRepository.findByStoryId(story.id)
            StoryResponse.fromStory(story, asset)
        }
    }


    fun getStoryDetails(storyId: Long): StoryResponse {
        val story = storyRepository.findById(storyId).orElseThrow { ResourceNotFoundException("Story not found") }
        val asset = storyAssetRepository.findByStoryId(story.id)
        return StoryResponse.fromStory(story, asset)
    }

    @Transactional
     fun recordStoryView(user: User, storyId: Long): StoryViewResponse {
        val story = storyRepository.findById(storyId).orElseThrow { ResourceNotFoundException("Story not found") }

        val existingView = storyViewRepository.findByStoryIdAndViewerId(storyId, user.id)
        if (existingView != null) {
            return StoryViewResponse.fromStoryView(existingView)
        }

        val storyView = StoryView(story = story, viewerId = user.id)
        val savedView = storyViewRepository.save(storyView)

        story.viewCount++
        storyRepository.save(story)

        return StoryViewResponse.fromStoryView(savedView)
    }

    @Transactional
     fun deleteStory(user: User, storyId: Long) {
        val story = storyRepository.findById(storyId).orElseThrow { ResourceNotFoundException("Story not found") }

        if (story.userId != user.id) {
            throw NotAllowedException("User does not own this story")
        }

        storyRepository.delete(story)
    }

    @Transactional
     fun createStoryGroup(user: User, request: CreateStoryGroupRequest): StoryGroupResponse {
        val profileStory = request.profileStoryId?.let {
            profileStoryRepository.findById(it).orElseThrow { ResourceNotFoundException("Profile story not found") }
        }

        val storyGroup = StoryGroup(
            profileStory = profileStory,
            title = request.title,
            imageUrl = request.imageUrl
        )
        val savedGroup = storyGroupRepository.save(storyGroup)

        return StoryGroupResponse.fromStoryGroup(savedGroup)
    }

     fun getStoryGroupDetails(groupId: Long): StoryGroupResponse {
        val group = storyGroupRepository.findById(groupId).orElseThrow { ResourceNotFoundException("Story group not found") }
        return StoryGroupResponse.fromStoryGroup(group)
    }

     fun getUserStoryGroups(userId: Long): List<StoryGroupResponse> {
        val profileStory = profileStoryRepository.findByUserId(userId) ?: throw ResourceNotFoundException("Profile story not found")
        val groups = storyGroupRepository.findByProfileStoryId(profileStory.id)
        return groups.map { StoryGroupResponse.fromStoryGroup(it) }
    }

    @Transactional
     fun deleteStoryGroup(user: User, groupId: Long) {
        val group = storyGroupRepository.findById(groupId).orElseThrow { ResourceNotFoundException("Story group not found") }

        if (group.profileStory?.userId != user.id) {
            throw NotAllowedException("User does not own this story group")
        }

        storyGroupRepository.delete(group)
    }

    suspend fun getDecryptedFileContent(storyId: Long): String {
        val asset = withContext(Dispatchers.IO) {
            storyAssetRepository.findByStoryId(storyId)
        }
            ?: throw ResourceNotFoundException("Story asset not found")

        return cryptoService.decrypt(
            asset.encryptedFilePath ?: throw ResourceNotFoundException("Encrypted file path not found"),
            asset.filePathEncryptedDataKey ?: throw ResourceNotFoundException("Encrypted data key not found")
        )
    }
}
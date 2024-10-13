package com.vemeet.backend.repository

import com.vemeet.backend.model.*
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface ProfileStoryRepository : JpaRepository<ProfileStory, Long> {
    fun findByUserId(userId: Long): ProfileStory?
}

@Repository
interface StoryGroupRepository : JpaRepository<StoryGroup, Long> {
    fun findByProfileStoryId(profileStoryId: Long): List<StoryGroup>
}

@Repository
interface StoryRepository : JpaRepository<Story, Long> {
    fun findByUserIdAndExpiresAtAfter(userId: Long, instant: Instant): List<Story>
    fun findByUserIdAndCreatedAtAfterAndExpiresAtAfter(
        userId: Long,
        createdAfter: Instant,
        expiresAfter: Instant
    ): List<Story>


}


@Repository
interface StoryViewRepository : JpaRepository<StoryView, Long> {
    fun findByStoryIdAndViewerId(storyId: Long, viewerId: Long): StoryView?
}

@Repository
interface StoryAssetRepository : JpaRepository<StoryAsset, Long> {
    fun findByStoryId(storyId: Long): StoryAsset?
    fun findByStoryIdIn(storyIds: List<Long>): List<StoryAsset>
}

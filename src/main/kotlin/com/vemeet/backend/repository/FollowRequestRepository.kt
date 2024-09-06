package com.vemeet.backend.repository

import com.vemeet.backend.model.FollowRequest
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FollowRequestRepository : JpaRepository<FollowRequest, Long> {
    fun findByRequesterIdAndTargetId(requesterId: Long, targetId: Long): FollowRequest?
    fun findByTargetId(targetId: Long): List<FollowRequest>
}
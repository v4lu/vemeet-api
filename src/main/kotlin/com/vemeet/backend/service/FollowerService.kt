package com.vemeet.backend.service


import com.vemeet.backend.dto.*
import com.vemeet.backend.model.FollowRequest
import com.vemeet.backend.model.Follower
import com.vemeet.backend.repository.FollowRequestRepository
import com.vemeet.backend.repository.FollowerRepository
import com.vemeet.backend.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class FollowerService(
    private val followerRepository: FollowerRepository,
    private val userRepository: UserRepository,
    private val followRequestRepository: FollowRequestRepository
) {

    @Transactional
    fun followUser(followerId: Long, followedId: Long): MessageFollowResponse {
        if (followerId == followedId) {
            throw IllegalArgumentException("A user cannot follow themselves")
        }

        val follower = userRepository.findById(followerId).orElseThrow { NoSuchElementException("Follower not found") }
        val followed = userRepository.findById(followedId).orElseThrow { NoSuchElementException("Followed user not found") }

        if (followed.isPrivate) {
            val existingRequest = followRequestRepository.findByRequesterIdAndTargetId(followerId, followedId)
            if (existingRequest != null) {
                throw IllegalStateException("Follow request already sent")
            }

            val newRequest = FollowRequest(requester = follower, target = followed)
            val savedRequest = followRequestRepository.save(newRequest)

            return MessageFollowResponse(
                message = "Requested follow for ${savedRequest.target}"
            )
        } else {
            val existingFollower = followerRepository.findByFollowerIdAndFollowedId(followerId, followedId)
            if (existingFollower != null) {
                throw IllegalStateException("Already following this user")
            }

            val newFollower = Follower(follower = follower, followed = followed)
            val savedFollower = followerRepository.save(newFollower)

            return MessageFollowResponse(
                message = "Followed ${savedFollower.follower}"
            )
        }
    }

    @Transactional
    fun unfollowUser(followerId: Long, followedId: Long) {
        val follower = followerRepository.findByFollowerIdAndFollowedId(followerId, followedId)
            ?: throw NoSuchElementException("Follower relationship not found")
        followerRepository.delete(follower)
    }

    fun getUserFollowers(userId: Long): List<UserResponse> {
        return followerRepository.findByFollowedId(userId).map {
            UserResponse.fromUser(it.follower)
        }
    }

    fun getUserFollowing(userId: Long): List<UserResponse> {
        return followerRepository.findByFollowerId(userId).map {
            UserResponse.fromUser(it.followed)
        }
    }

    fun getUserFollowStats(userId: Long): UserFollowStatsResponse {
        val user = userRepository.findById(userId).orElseThrow { NoSuchElementException("User not found") }
        val followerCount = followerRepository.countByFollowedId(userId)
        val followingCount = followerRepository.countByFollowerId(userId)

        return UserFollowStatsResponse(userId, user.username, followerCount, followingCount)
    }


    @Transactional
    fun acceptFollowRequest(requestId: Long, userId: Long) {
        val request = followRequestRepository.findById(requestId)
            .orElseThrow { NoSuchElementException("Follow request not found") }

        if (request.target.id != userId) {
            throw IllegalArgumentException("User is not authorized to accept this request")
        }

        val newFollower = Follower(follower = request.requester, followed = request.target)
        followerRepository.save(newFollower)
        followRequestRepository.delete(request)
    }

    @Transactional
    fun rejectFollowRequest(requestId: Long, userId: Long) {
        val request = followRequestRepository.findById(requestId)
            .orElseThrow { NoSuchElementException("Follow request not found") }

        if (request.target.id != userId) {
            throw IllegalArgumentException("User is not authorized to reject this request")
        }

        followRequestRepository.delete(request)
    }

    fun getPendingFollowRequests(userId: Long): List<FollowRequestResponse> {
        return followRequestRepository.findByTargetId(userId).map {
            FollowRequestResponse(
                id = it.id,
                requesterUsername = it.requester.username,
                targetUsername = it.target.username,
                createdAt = DateTimeFormatter.ISO_INSTANT.format(it.createdAt)
            )
        }
    }
}
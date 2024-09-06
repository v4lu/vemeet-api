package com.vemeet.backend.repository

import com.vemeet.backend.model.Follower
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface FollowerRepository : JpaRepository<Follower, Long> {
    fun findByFollowerIdAndFollowedId(followerId: Long, followedId: Long): Follower?
    fun countByFollowerId(followerId: Long): Int
    fun countByFollowedId(followedId: Long): Int
    fun findByFollowerId(followerId: Long): List<Follower>
    fun findByFollowedId(followedId: Long): List<Follower>
    fun existsByFollowerIdAndFollowedId(followerId: Long, followedId: Long): Boolean
}
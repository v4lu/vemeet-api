package com.vemeet.backend.repository

import com.vemeet.backend.model.Match
import com.vemeet.backend.model.Swipe
import com.vemeet.backend.model.User
import com.vemeet.backend.model.UserPreference
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository


@Repository
interface SwipeRepository : JpaRepository<Swipe, Long> {
    fun findBySwiperAndSwipedAndDirection(swiper: User, swiped: User, direction: String): Swipe?
}

@Repository
interface MatchRepository : JpaRepository<Match, Long> {
    fun findByUser1AndUser2(user1: User, user2: User): Match?
    fun findByUser1OrUser2(user1: User, user2: User): List<Match>
}

@Repository
interface UserPreferenceRepository : JpaRepository<UserPreference, Long> {
    fun findByUserId(userId: Long): UserPreference?
}
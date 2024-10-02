package com.vemeet.backend.service


import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.Match
import com.vemeet.backend.model.Swipe
import com.vemeet.backend.model.User
import com.vemeet.backend.repository.MatchRepository
import com.vemeet.backend.repository.SwipeRepository
import com.vemeet.backend.repository.UserPreferenceRepository
import com.vemeet.backend.repository.UserRepository
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class SwipeService(
    private val swipeRepository: SwipeRepository,
    private val userRepository: UserRepository,
    private val matchRepository: MatchRepository,
    private val entityManager: EntityManager,
    private val userPreferenceRepository: UserPreferenceRepository
) {

    val logger = LoggerFactory.getLogger(SwipeService::class.java)

    fun getMatches(user: User): List<UserResponse> {
        val matches = matchRepository.findByUser1OrUser2(user, user)

        return matches.map { match ->
            val otherUser = if (match.user1.id == user.id) match.user2 else match.user1
            UserResponse.fromUser(otherUser)
        }
    }

    @Transactional
    fun createSwipe(swiper: User, request: SwipeRequest): SwipeResponse {
        val swipedUser = userRepository.findById(request.swipedUserId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val swipe = Swipe(
            swiper = swiper,
            swiped = swipedUser,
            direction = request.direction
        )

        val savedSwipe = swipeRepository.save(swipe)

        val isMatch = if (request.direction == "right") {
            checkForMatch(swiper, swipedUser)
        } else false

        return SwipeResponse.fromSwipe(savedSwipe, isMatch)
    }

    private fun checkForMatch(user1: User, user2: User): Boolean {
        val existingSwipe = swipeRepository.findBySwiperAndSwipedAndDirection(user2, user1, "right")
        if (existingSwipe != null) {
            val match = Match(user1 = user1, user2 = user2)
            matchRepository.save(match)
            return true
        }
        return false
    }

    fun getPotentialMatches(user: User, page: Int, size: Int): PaginatedPotentialMatches {
        val userPreferences = userPreferenceRepository.findByUserId(user.id)
        if (userPreferences == null) {
            logger.warn("No preferences found for user ${user.id}")
            return PaginatedPotentialMatches(emptyList(), false)
        }
        if (user.cityLat == null || user.cityLng == null) {
            logger.warn("User ${user.id} has no location data")
            return PaginatedPotentialMatches(emptyList(), false)
        }

        val query = entityManager.createNativeQuery("""
        SELECT pm.potential_match_id, pm.distance
        FROM potential_matches pm
        WHERE pm.user_id = :userId
        ORDER BY RANDOM()
        LIMIT :limit OFFSET :offset
    """)
        query.setParameter("userId", user.id)
        query.setParameter("limit", size + 1) // Fetch one extra to check for next page
        query.setParameter("offset", page * size)

        val results = query.resultList as List<Array<Any?>>

        logger.debug("Raw results for user ${user.id}: $results")

        val potentialMatches = results.mapNotNull { result ->
            try {
                val potentialMatchId = (result[0] as? Number)?.toLong()
                    ?: throw IllegalStateException("Invalid potential_match_id")
                val distance = (result[1] as? Number)?.toDouble()
                    ?: throw IllegalStateException("Invalid distance")

                val potentialMatchUser = userRepository.findById(potentialMatchId)
                    .orElseThrow { ResourceNotFoundException("User not found for ID: $potentialMatchId") }

                PotentialMatchResponse(
                    userId = potentialMatchId,
                    distance = distance,
                    user = UserResponse.fromUser(potentialMatchUser)
                )
            } catch (e: Exception) {
                logger.error("Error processing potential match for user ${user.id}: ${e.message}")
                null
            }
        }

        val hasNextPage = potentialMatches.size > size
        val paginatedMatches = potentialMatches.take(size)

        logger.debug("Found ${paginatedMatches.size} potential matches for user ${user.id} on page $page. Has next page: $hasNextPage")

        return PaginatedPotentialMatches(paginatedMatches, hasNextPage)
    }
}
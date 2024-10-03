package com.vemeet.backend.service


import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.Match
import com.vemeet.backend.model.Swipe
import com.vemeet.backend.model.SwiperUserProfile
import com.vemeet.backend.model.User
import com.vemeet.backend.repository.*
import jakarta.persistence.EntityManager
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class SwipeService(
    private val swipeRepository: SwipeRepository,
    private val userRepository: UserRepository,
    private val matchRepository: MatchRepository,
    private val entityManager: EntityManager,
    private val userPreferenceRepository: UserPreferenceRepository,
    private val swiperUserProfileRepository : SwipeUserProfile
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

        val potentialMatches = results.mapNotNull { result ->
            try {
                val potentialMatchId = (result[0] as? Number)?.toLong()
                    ?: throw IllegalStateException("Invalid potential_match_id")
                val distance = (result[1] as? Number)?.toDouble()
                    ?: throw IllegalStateException("Invalid distance")

                val potentialMatchUser = userRepository.findById(potentialMatchId)
                    .orElseThrow { ResourceNotFoundException("User not found for ID: $potentialMatchId") }

                val swiperUserProfile = swiperUserProfileRepository.findByUserId(potentialMatchId)
                if (swiperUserProfile == null) {
                    logger.warn("SwiperUserProfile not found for user ID: $potentialMatchId. Skipping this potential match.")
                    return@mapNotNull null
                }

                val userResponse = UserResponse.fromUser(potentialMatchUser)
                val swiperUserProfileResponse = SwiperUserProfileResponse.fromSwiperUserProfile(swiperUserProfile, userResponse)

                PotentialMatchResponse(
                    userId = potentialMatchId,
                    distance = distance,
                    swiperUserProfile = swiperUserProfileResponse
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

        fun getProfileByUserId(userId: Long): SwiperUserProfileResponse {
        val profile = swiperUserProfileRepository.findByUserId(userId)
            ?: throw ResourceNotFoundException("Profile not found for user with id: $userId")

        val user = userRepository.findById(userId)
            .orElseThrow { ResourceNotFoundException("User not found with id: $userId") }

        val userResponse = UserResponse.fromUser(user)

        return SwiperUserProfileResponse.fromSwiperUserProfile(profile, userResponse)
    }

    @Transactional
    fun updateProfile(user: User, request: SwiperUserProfileRequest): SwiperUserProfileResponse {
        val profile = swiperUserProfileRepository.findByUserId(user.id)
            ?: throw ResourceNotFoundException("Profile not found for user with id: $user.id")

        profile.apply {
            description = request.description ?: description
            mainImageUrl = request.mainImageUrl ?: mainImageUrl
            otherImages = request.otherImages
            updatedAt = Instant.now()
        }

        val savedProfile = swiperUserProfileRepository.save(profile)
        return SwiperUserProfileResponse.fromSwiperUserProfile(savedProfile, UserResponse.fromUser(user))
    }



}
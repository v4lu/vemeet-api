package com.vemeet.backend.service


import com.vemeet.backend.dto.*
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.*
import com.vemeet.backend.repository.*
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class SwipeService(
    private val swipeRepository: SwipeRepository,
    private val userRepository: UserRepository,
    private val matchRepository: MatchRepository,
    private val swiperUserProfileRepository : SwipeUserProfile,
    private val potentialMatchRepository: PotentialMatchRepository,
    private val notificationService: NotificationService
) {

    fun getMatches(user: User): List<SwiperUserProfileResponse> {
        val matches = matchRepository.findByUser1OrUser2(user, user)

        return matches.map { match ->
            val matchUser = if (match.user1.id == user.id) match.user2 else match.user1

            val profile = swiperUserProfileRepository.findByUserId(matchUser.id)
                ?: throw ResourceNotFoundException("Profile not found for user with id: ${matchUser.id}")

            val userResponse = UserResponse.fromUser(matchUser)

            SwiperUserProfileResponse.fromSwiperUserProfile(profile, userResponse)
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


        notificationService.createNotification(
            request.swipedUserId,
            NotificationTypeEnum.NEW_MATCH.typeName,
            "${swiper.username} liked you!"
        )

        return SwipeResponse.fromSwipe(savedSwipe, isMatch)
    }

    fun getPotentialMatches(user: User, pageable: Pageable): Page<SwiperPotencialUserProfileResponse> {
        val potentialMatches = potentialMatchRepository.findByIdUserId(user.id, pageable)

        return potentialMatches.map { potentialMatch ->
            val profile = swiperUserProfileRepository.findByUserId(potentialMatch.id.potentialMatchId)
                ?: throw ResourceNotFoundException("Profile not found for user with id: ${potentialMatch.id.potentialMatchId}")

            val matchUser = userRepository.findById(potentialMatch.id.potentialMatchId)
                .orElseThrow { ResourceNotFoundException("User not found with id: ${potentialMatch.id.potentialMatchId}") }

            val userResponse = UserResponse.fromUser(matchUser)

            SwiperPotencialUserProfileResponse.fromSwiperUserProfile(profile, userResponse, potentialMatch)
        }
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


    private fun checkForMatch(user1: User, user2: User): Boolean {
        val existingSwipe = swipeRepository.findBySwiperAndSwipedAndDirection(user2, user1, "right")
        if (existingSwipe != null) {
            val match = Match(user1 = user1, user2 = user2)
            matchRepository.save(match)

            notificationService.createNotification(
                user1.id,
                NotificationTypeEnum.NEW_MATCH.typeName,
                "${user2.username} is a match!"
            )

            notificationService.createNotification(
                user2.id,
                NotificationTypeEnum.NEW_MATCH.typeName,
                "${user1.username} is a match!"
            )
            return true
        }
        return false
    }
}
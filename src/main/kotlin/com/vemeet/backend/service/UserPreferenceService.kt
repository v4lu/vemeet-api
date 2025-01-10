package com.vemeet.backend.service


import com.vemeet.backend.dto.UserPreferenceRequest
import com.vemeet.backend.dto.UserPreferenceResponse
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.SwiperUserProfile
import com.vemeet.backend.model.User
import com.vemeet.backend.model.UserPreference
import com.vemeet.backend.repository.SwipeUserProfile
import com.vemeet.backend.repository.UserPreferenceRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class UserPreferenceService(
    private val userPreferenceRepository: UserPreferenceRepository,
    private val swiperUserProfileRepository: SwipeUserProfile,
) {

    fun getUserPreference(user: User): UserPreferenceResponse {
        val preference = userPreferenceRepository.findByUserId(user.id)
            ?: throw ResourceNotFoundException("Preference not found for user ${user.id}")
        return UserPreferenceResponse(
            id = preference.id,
            userId = preference.user.id,
            minAge = preference.minAge,
            maxAge = preference.maxAge,
            preferredGender = preference.preferredGender,
            maxDistance = preference.maxDistance
        )
    }

    @Transactional
    fun createUserPreference(user: User, request: UserPreferenceRequest): UserPreferenceResponse {
        val preference = UserPreference(
            user = user,
            minAge = request.minAge,
            maxAge = request.maxAge,
            preferredGender = request.preferredGender,
            maxDistance = request.maxDistance
        )

        val profile = SwiperUserProfile(
            userId = user.id,
            mainImageUrl = user.profileImage?.url
        )
        swiperUserProfileRepository.save(profile)
        val savedPreference = userPreferenceRepository.save(preference)
        return UserPreferenceResponse(
            id = savedPreference.id,
            userId = savedPreference.user.id,
            minAge = savedPreference.minAge ,
            maxAge = savedPreference.maxAge ,
            preferredGender = savedPreference.preferredGender,
            maxDistance = savedPreference.maxDistance
        )
    }

    @Transactional
    fun updateUserPreference(user: User, request: UserPreferenceRequest): UserPreferenceResponse {
        val preference = userPreferenceRepository.findByUserId(user.id)
            ?: throw ResourceNotFoundException("Preference not found for user ${user.id}")

        preference.apply {
            minAge = request.minAge
            maxAge = request.maxAge
            preferredGender = request.preferredGender
            maxDistance = request.maxDistance
            updatedAt = Instant.now()
        }

        val updatedPreference = userPreferenceRepository.save(preference)
        return UserPreferenceResponse(
            id = updatedPreference.id,
            userId = updatedPreference.user.id,
            minAge = updatedPreference.minAge,
            maxAge = updatedPreference.maxAge,
            preferredGender = updatedPreference.preferredGender,
            maxDistance = updatedPreference.maxDistance
        )
    }

    @Transactional
    fun deleteUserPreference(user: User) {
        val preference = userPreferenceRepository.findByUserId(user.id)
            ?: throw ResourceNotFoundException("Preference not found for user ${user.id}")
        userPreferenceRepository.delete(preference)
    }
}
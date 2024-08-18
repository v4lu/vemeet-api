package com.vemeet.backend.service

import com.vemeet.backend.dto.SessionResponse
import com.vemeet.backend.exception.ResourceNotFoundException
import com.vemeet.backend.model.User
import com.vemeet.backend.repository.UserRepository
import com.vemeet.backend.exception.UnauthorizedException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userCacheService: UserCacheService,
    private val cognitoService: CognitoService
) {

    @Transactional(readOnly = true)
    fun getUserSession(accessToken: String): SessionResponse {
        // Try to get user from cache
        val cachedUser = userCacheService.getUserByToken(accessToken)

        if (cachedUser != null) {
            return mapToUserSessionResponse(cachedUser, true)
        }

        // If not in cache, get from Cognito and database
        val cognitoId = try {
            cognitoService.getUserSub(accessToken)
        } catch (e: Exception) {
            throw UnauthorizedException("Invalid access token")
        }

        val user = findByAwsCognitoId(cognitoId)
            ?: throw ResourceNotFoundException("User not found")

        // Cache the user for future requests
        userCacheService.setUserByToken(accessToken, 3600, user) // Cache for 1 hour

        return mapToUserSessionResponse(user, false)
    }

    @Transactional(readOnly = true)
    fun findByAwsCognitoId(awsCognitoId: String): User? {
        return userRepository.findByAwsCognitoId(awsCognitoId)
    }

    @Transactional(readOnly = true)
    fun findByUsername(username: String): User? {
        return userRepository.findByUsername(username)
    }

    @Transactional
    fun createUser(username: String, birthday: LocalDate, awsCognitoId: String): User {
        val user = User(
            username = username,
            birthday = birthday,
            awsCognitoId = awsCognitoId
        )
        return userRepository.save(user)
    }

    @Transactional
    fun updateUser(user: User): User {
        return userRepository.save(user)
    }

    private fun mapToUserSessionResponse(user: User, isCached: Boolean): SessionResponse {
        return SessionResponse(
            id = user.id,
            username = user.username,
            birthday = user.birthday,
            awsCognitoId = user.awsCognitoId,
            verified = user.verified,
            isPrivate = user.isPrivate,
            lockedInbox = user.inboxLocked,
        )
    }
}
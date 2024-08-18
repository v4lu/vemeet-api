package com.vemeet.backend.service

import com.amazonaws.services.cognitoidp.model.UserNotFoundException
import com.vemeet.backend.dto.LoginResponse
import com.vemeet.backend.exception.BadRequestException
import com.vemeet.backend.exception.InvalidCredentialsException
import com.vemeet.backend.model.User
import com.vemeet.backend.repository.UserRepository
import jakarta.transaction.Transactional
import org.springframework.stereotype.Service
import java.time.Instant
import java.time.LocalDate
import java.time.temporal.ChronoUnit

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val cognitoService: CognitoService
) {

    fun createUser(username: String, birthday: LocalDate, awsCognitoId: String): User {
        if (userRepository.findByUsername(username) != null) {
            throw BadRequestException("Username already exists")
        }

        val user = User(
            username = username,
            birthday = birthday,
            awsCognitoId = awsCognitoId
        )
        return userRepository.save(user)
    }

    fun login(email: String, password: String): LoginResponse {
        val authResult = try {
            cognitoService.initiateAuth(email, password)
        } catch (e: Exception) {
            throw InvalidCredentialsException("Invalid email or password")
        }

        val cognitoId = try {
            cognitoService.getUserSub(authResult.authenticationResult.accessToken)
        } catch (e: Exception) {
            throw UserNotFoundException("User not found")
        }

        val user = findByAwsCognitoId(cognitoId)
            ?: throw UserNotFoundException("User not found in the database")

        val now = Instant.now()
        val thirtyDaysLater = now.plus(30, ChronoUnit.DAYS)

        return LoginResponse(
            cognitoId = cognitoId,
            refreshToken = authResult.authenticationResult.refreshToken,
            refreshTokenExpiry = thirtyDaysLater,
            accessToken = authResult.authenticationResult.accessToken,
            accessTokenExpiry = now.plusSeconds(authResult.authenticationResult.expiresIn.toLong())
        )
    }

    @Transactional
    fun markUserAsVerified(id: String) {
        val user = userRepository.findByAwsCognitoId(id) ?: throw UserNotFoundException("User not found with")
        user.verified = true
        userRepository.save(user)
    }

    fun findByUsername(username: String): User? = userRepository.findByUsername(username)
    fun findByAwsCognitoId(awsCognitoId: String): User? =  userRepository.findByAwsCognitoId(awsCognitoId)

}
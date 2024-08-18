package com.vemeet.backend.service

import com.vemeet.backend.model.User
import com.vemeet.backend.repository.UserRepository
import org.springframework.stereotype.Service
import java.time.LocalDate

@Service
class UserService(private val userRepository: UserRepository) {

    fun createUser(username: String, birthday: LocalDate, awsCognitoId: String): User {
        val user = User(
            username = username,
            birthday = birthday,
            awsCognitoId = awsCognitoId
        )
        return userRepository.save(user)
    }


  fun findByAwsCognitoId(awsCognitoId: String): User? {
        return userRepository.findByAwsCognitoId(awsCognitoId)
    }
}
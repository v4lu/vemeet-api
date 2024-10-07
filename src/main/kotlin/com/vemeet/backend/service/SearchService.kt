package com.vemeet.backend.service

import com.vemeet.backend.dto.UserResponse
import com.vemeet.backend.repository.UserRepository
import org.springframework.stereotype.Service


@Service
class SearchService(private val userRepository: UserRepository) {

    fun searchUsers(query: String): List<UserResponse> {
        val users = userRepository.searchByUsernameOrName(query)
        return users.map { UserResponse.fromUser(it) }
    }

}

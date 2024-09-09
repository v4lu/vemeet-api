package com.vemeet.backend.controller

import com.vemeet.backend.model.User
import com.vemeet.backend.repository.UserRepository
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/hello")
class HelloController (
    private val userRepository: UserRepository
) {
    private val logger = LoggerFactory.getLogger(HelloController::class.java)
    @GetMapping
    fun hello(): String {
        logger.info("This is an info log")
        logger.error("This is an error log")
        return "Hello World!"
    }

    @GetMapping("/users")
    fun users(): ResponseEntity<List<User>> {
       val users = userRepository.findAll()

       return ResponseEntity.ok(users)
    }
}
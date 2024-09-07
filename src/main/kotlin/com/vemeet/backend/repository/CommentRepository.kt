package com.vemeet.backend.repository

import com.vemeet.backend.model.Comment
import org.springframework.data.jpa.repository.JpaRepository

interface CommentRepository: JpaRepository<Comment, Long> {
}
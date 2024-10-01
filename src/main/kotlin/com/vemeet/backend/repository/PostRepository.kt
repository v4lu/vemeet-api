package com.vemeet.backend.repository

import com.vemeet.backend.model.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<Post, Long> {

    fun findAllByUserIdOrderByCreatedAtDesc(userId: Long, pageable: Pageable): Page<Post>

    @Query("""
        SELECT p FROM Post p
        WHERE p.userId = :userId
           OR p.userId IN (
               SELECT f.followed.id
               FROM Follower f
               WHERE f.follower.id = :userId
           )
        ORDER BY p.createdAt DESC
    """)
    fun findFeedPostsForUser(userId: Long, pageable: Pageable): Page<Post>

    @Query("""
        SELECT p FROM Post p
        JOIN User u ON p.userId = u.id
        WHERE p.userId = :userId OR
              (u.isPrivate = false) OR
              (u.isPrivate = true AND EXISTS (
                  SELECT f FROM Follower f
                  WHERE f.followed.id = p.userId AND f.follower.id = :userId
              ))
        ORDER BY p.createdAt DESC
    """)
    fun findVisiblePosts(userId: Long, pageable: Pageable): Page<Post>
}
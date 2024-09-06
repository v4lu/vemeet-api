package com.vemeet.backend.repository

import com.vemeet.backend.model.Post
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository

@Repository
interface PostRepository : JpaRepository<Post, Long> {

    @Query("""
        SELECT p FROM Post p
        WHERE p.user.id = :userId OR
              (p.user.isPrivate = false) OR
              (p.user.isPrivate = true AND EXISTS (
                  SELECT f FROM Follower f
                  WHERE f.followed.id = p.user.id AND f.follower.id = :userId
              ))
        ORDER BY p.createdAt DESC
    """)
    fun findVisiblePosts(userId: Long, pageable: Pageable): Page<Post>

    @Query("""
        SELECT p FROM Post p
        WHERE p.user.id = :userId
        ORDER BY p.createdAt DESC
    """)
    fun findPostsByUserId(userId: Long, pageable: Pageable): Page<Post>

    @Query("""
        SELECT COUNT(p) FROM Post p
        WHERE p.user.id = :userId
    """)
    fun countPostsByUserId(userId: Long): Long

    @Query("""
        SELECT p FROM Post p
        WHERE p.id IN (
            SELECT r.post.id FROM Reaction r
            WHERE r.user.id = :userId
        )
        ORDER BY p.createdAt DESC
    """)
    fun findPostsReactedByUser(userId: Long, pageable: Pageable): Page<Post>

    @Query("""
        SELECT p FROM Post p
        WHERE p.content LIKE %:keyword% OR
              p.user.username LIKE %:keyword% OR
              p.user.name LIKE %:keyword%
        ORDER BY p.createdAt DESC
    """)
    fun searchPosts(keyword: String, pageable: Pageable): Page<Post>
}
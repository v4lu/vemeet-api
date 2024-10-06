package com.vemeet.backend.repository

import com.vemeet.backend.model.Recipe
import com.vemeet.backend.model.RecipeImage
import com.vemeet.backend.model.User
import io.lettuce.core.dynamic.annotation.Param
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository
import java.time.Instant

@Repository
interface RecipeRepository : JpaRepository<Recipe, Long> {
     override fun findAll(pageable: Pageable): Page<Recipe>

     fun findByUser(user: User, pageable: Pageable): Page<Recipe>

    @Query("""
        SELECT r FROM Recipe r
        LEFT JOIN r.category c
        LEFT JOIN r.tags t
        WHERE (:title IS NULL OR LOWER(r.title) LIKE LOWER(CONCAT('%', :title, '%')))
        AND (:categoryId IS NULL OR c.id = :categoryId)
        AND (:tagId IS NULL OR :tagId IN (SELECT t.id FROM r.tags t))
        AND (:difficulty IS NULL OR r.difficulty = :difficulty)
        AND (:minServings IS NULL OR r.servings >= :minServings)
        AND (:maxServings IS NULL OR r.servings <= :maxServings)
        AND (:createdAfter IS NULL OR r.createdAt >= :createdAfter)
        AND (:createdBefore IS NULL OR r.createdAt <= :createdBefore)
    """)
    fun findAllWithFilters(
        @Param("title") title: String?,
        @Param("categoryId") categoryId: Long?,
        @Param("tagId") tagId: Long?,
        @Param("difficulty") difficulty: String?,
        @Param("minServings") minServings: Int?,
        @Param("maxServings") maxServings: Int?,
        @Param("createdAfter") createdAfter: Instant?,
        @Param("createdBefore") createdBefore: Instant?,
        pageable: Pageable
    ): Page<Recipe>

    @Query("""
        SELECT r FROM Recipe r
        WHERE r.user.id = :userId
           OR r.user.id IN (
               SELECT f.followed.id
               FROM Follower f
               WHERE f.follower.id = :userId
           )
        ORDER BY r.createdAt DESC
    """)
    fun findFeedRecipesForUser(userId: Long, pageable: Pageable): Page<Recipe>
}

@Repository
interface RecipeImageRepository: JpaRepository<RecipeImage, Long> {}
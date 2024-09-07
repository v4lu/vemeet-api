package com.vemeet.backend.repository

import com.vemeet.backend.model.RecipeCategory
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RecipeCategoryRepository : JpaRepository<RecipeCategory, Long>
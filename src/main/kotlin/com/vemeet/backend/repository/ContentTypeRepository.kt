package com.vemeet.backend.repository

import com.vemeet.backend.model.ContentType
import org.springframework.data.jpa.repository.JpaRepository
import java.util.Optional

interface ContentTypeRepository: JpaRepository<ContentType, Long> {
    fun findByName(contentType: String): ContentType?
}

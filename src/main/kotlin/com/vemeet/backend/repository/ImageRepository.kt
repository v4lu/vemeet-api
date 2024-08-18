package com.vemeet.backend.repository

import com.vemeet.backend.model.Image
import org.springframework.data.jpa.repository.JpaRepository

interface ImageRepository: JpaRepository<Image, Long>
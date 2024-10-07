package com.vemeet.backend.repository

import com.vemeet.backend.model.Country
import org.springframework.data.jpa.repository.JpaRepository

interface CountryRepository: JpaRepository<Country, Long> {
}
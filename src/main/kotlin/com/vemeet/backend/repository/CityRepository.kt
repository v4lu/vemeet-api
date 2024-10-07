package com.vemeet.backend.repository

import com.vemeet.backend.model.City
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query


interface CityRepository : JpaRepository<City, Long> {
    @Query("SELECT c FROM City c WHERE c.countryIsoCode = :countryIsoCode AND LOWER(c.cityName) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    fun findByCountryIsoCodeAndCityNameContaining(countryIsoCode: String, searchTerm: String): List<City>
}

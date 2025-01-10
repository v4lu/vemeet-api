package com.vemeet.backend.service

import com.vemeet.backend.model.City
import com.vemeet.backend.model.Country
import com.vemeet.backend.repository.CityRepository
import com.vemeet.backend.repository.CountryRepository
import org.springframework.stereotype.Service

@Service
class LocationService(
    private val cityRepository: CityRepository,
    private val countryRepository: CountryRepository
) {

    fun getAllCountries(): List<Country> {
        return countryRepository.findAll()
    }

    fun searchCitiesByCountryAndName(countryIsoCode: String, searchTerm: String): List<City> {
        return cityRepository.findByCountryIsoCodeAndCityNameContaining(countryIsoCode, searchTerm)
    }
}
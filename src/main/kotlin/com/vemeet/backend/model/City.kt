package com.vemeet.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "cities")
data class City(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "country_iso_code")
    val countryIsoCode: String = "",

    @Column(name = "city_name")
    val cityName: String = "",

    @Column(name = "city_lat")
    val cityLat: Double = 0.0,

    @Column(name = "city_lng")
    val cityLng: Double = 0.0,
)
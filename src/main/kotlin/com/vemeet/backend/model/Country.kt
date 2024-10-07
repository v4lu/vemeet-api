package com.vemeet.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "countries")
data class Country(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "country_name")
    val countryName: String = "",

    @Column(name = "country_flag")
    val countryFlag: String = "",

    @Column(name = "country_iso_code")
    val countryIsoCode: String = "",

    @Column(name = "country_lat")
    val countryLat: Double = 0.0,

    @Column(name = "country_lng")
    val countryLng: Double = 0.0,
)
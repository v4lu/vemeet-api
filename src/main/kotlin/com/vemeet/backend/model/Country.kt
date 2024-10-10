package com.vemeet.backend.model

import jakarta.persistence.*

@Entity
@Table(name = "countries")
data class Country(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Int = 0,

    @Column(name = "name")
    val countryName: String = "",

    @Column(name = "flag")
    val countryFlag: String = "",

    @Column(name = "iso_code")
    val countryIsoCode: String = "",

    @Column(name = "lat")
    val countryLat: Double = 0.0,

    @Column(name = "lng")
    val countryLng: Double = 0.0,
)
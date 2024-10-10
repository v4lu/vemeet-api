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

    @Column(name = "name")
    val cityName: String = "",

    @Column(name = "lat")
    val cityLat: Double = 0.0,

    @Column(name = "lng")
    val cityLng: Double = 0.0,

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "country_id", referencedColumnName = "id")
    var country: Country = Country(),
)
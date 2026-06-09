package com.example.myapplication.fake

import com.example.myapplication.data.api.CountriesApi
import com.example.myapplication.data.dto.CountryDto
import com.example.myapplication.data.dto.FlagsDto
import com.example.myapplication.data.dto.NameDto

class FakeCountriesApi(
    private val countries: List<CountryDto> = defaultCountries,
    private val failAll: Boolean = false,
    private val failOnce: Boolean = false
) : CountriesApi {

    private var allRequestCount = 0

    override suspend fun getCountries(): List<CountryDto> {
        allRequestCount++
        if (failAll) throw java.io.IOException("API unavailable")
        if (failOnce && allRequestCount == 1) throw java.io.IOException("API unavailable")
        return countries
    }

    override suspend fun getCountry(code: String): List<CountryDto> {
        return countries.filter { it.cca2 == code }
    }

    companion object {
        val defaultCountries = listOf(
            CountryDto(
                name = NameDto("Ukraine"),
                cca2 = "UA",
                capital = listOf("Kyiv"),
                region = "Europe",
                population = 40_000_000L,
                flags = FlagsDto("https://example.com/ua.png")
            ),
            CountryDto(
                name = NameDto("Poland"),
                cca2 = "PL",
                capital = listOf("Warsaw"),
                region = "Europe",
                population = 38_000_000L,
                flags = FlagsDto("https://example.com/pl.png")
            )
        )
    }
}

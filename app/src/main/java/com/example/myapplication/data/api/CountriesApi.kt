package com.example.myapplication.data.api


import com.example.myapplication.data.dto.CountryDto
import retrofit2.http.GET
import retrofit2.http.Path

interface CountriesApi {

    @GET("all?fields=name,cca2,flags,capital,region,population")
    suspend fun getCountries(): List<CountryDto>

    @GET("alpha/{code}")
    suspend fun getCountry(@Path("code") code: String): List<CountryDto>

}
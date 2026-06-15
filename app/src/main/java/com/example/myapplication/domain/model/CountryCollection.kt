package com.example.myapplication.domain.model

data class CountryCollection(
    val id: Long,
    val name: String,
    val createdAt: Long,
    val countryCount: Int = 0
)

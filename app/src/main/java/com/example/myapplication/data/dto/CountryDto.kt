package com.example.myapplication.data.dto

data class CountryDto(
    val name: NameDto,
    val cca2: String,
    val capital: List<String>?,
    val region: String?,
    val population: Long?,
    val flags: FlagsDto
)

data class NameDto(
    val common: String
)

data class FlagsDto(
    val png: String
)
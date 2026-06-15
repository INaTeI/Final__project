package com.example.myapplication.domain.repository

import com.example.myapplication.domain.model.Country
import kotlinx.coroutines.flow.Flow

interface CountryRepository {
    fun observeCountries(): Flow<List<Country>>
    suspend fun refreshCountries()
    suspend fun getCountry(code: String): Country
    suspend fun hasCachedCountries(): Boolean
    suspend fun getLastCacheTimestamp(): Long
}

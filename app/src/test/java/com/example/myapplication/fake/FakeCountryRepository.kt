package com.example.myapplication.fake

import com.example.myapplication.domain.model.Country
import com.example.myapplication.domain.repository.CountryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.IOException

class FakeCountryRepository : CountryRepository {

    private val countriesFlow = MutableStateFlow<List<Country>>(emptyList())

    var refreshCallCount = 0
        private set
    var shouldFailRefresh = false

    override fun observeCountries(): Flow<List<Country>> = countriesFlow.asStateFlow()

    override suspend fun refreshCountries() {
        refreshCallCount++
        if (shouldFailRefresh) {
            throw IOException("Network error")
        }
    }

    var getCountryOverride: (suspend (String) -> Country)? = null

    override suspend fun getCountry(code: String): Country {
        getCountryOverride?.let { return it(code) }
        return countriesFlow.value.first { it.code == code }
    }

    override suspend fun hasCachedCountries(): Boolean = countriesFlow.value.isNotEmpty()

    override suspend fun getLastCacheTimestamp(): Long = 0L

    fun setCountries(countries: List<Country>) {
        countriesFlow.value = countries
    }

    fun emitCountries(countries: List<Country>) {
        countriesFlow.value = countries
    }
}

package com.example.myapplication.data.repository

import com.example.myapplication.data.api.CountriesApi
import com.example.myapplication.data.dto.CountryDto
import com.example.myapplication.data.local.CountriesDao
import com.example.myapplication.data.local.CountryEntity
import com.example.myapplication.domain.model.Country
import com.example.myapplication.domain.preferences.AppPreferences
import com.example.myapplication.domain.repository.CountryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CountryRepositoryImpl @Inject constructor(
    private val api: CountriesApi,
    private val dao: CountriesDao,
    private val preferences: AppPreferences
) : CountryRepository {

    override fun observeCountries(): Flow<List<Country>> {
        return dao.observeAll().map { list -> list.map { it.toCountry() } }
    }

    override suspend fun refreshCountries() {
        val now = System.currentTimeMillis()
        val countries = api.getCountries().map { it.toCountry() }
        dao.insertAll(countries.map { CountryEntity.fromCountry(it, now) })
        preferences.setLastSyncTimestamp(now)
    }

    override suspend fun getCountry(code: String): Country {
        dao.getByCode(code)?.toCountry()?.let { return it }

        val dto = api.getCountry(code).first()
        val country = dto.toCountry()
        dao.insert(CountryEntity.fromCountry(country))
        return country
    }

    override suspend fun hasCachedCountries(): Boolean {
        return dao.getAll().isNotEmpty()
    }

    override suspend fun getLastCacheTimestamp(): Long {
        return dao.getLastCacheTimestamp() ?: preferences.getLastSyncTimestamp()
    }

    private fun CountryDto.toCountry() = Country(
        name = name.common,
        code = cca2,
        capital = capital?.firstOrNull() ?: "Unknown",
        region = region ?: "",
        population = population ?: 0,
        flag = flags.png
    )
}

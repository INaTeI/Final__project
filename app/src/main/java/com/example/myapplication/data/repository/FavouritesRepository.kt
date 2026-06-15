package com.example.myapplication.data.repository

import com.example.myapplication.data.local.FavouriteCountryEntity
import com.example.myapplication.data.local.FavouritesDao
import com.example.myapplication.domain.model.Country
import com.example.myapplication.domain.model.FavouriteCountry
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class FavouritesRepository @Inject constructor(
    private val dao: FavouritesDao,
    private val profileRepository: ProfileRepository
) {

    fun observeFavourites(): Flow<List<FavouriteCountry>> =
        profileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            dao.observeForProfile(profileId).map { list ->
                list.map { it.toFavouriteCountry() }
            }
        }

    fun observeFavouriteCodes(): Flow<Set<String>> =
        observeFavourites().map { list -> list.map { it.country.code }.toSet() }

    suspend fun add(country: Country) {
        val profileId = profileRepository.ensureDefaultProfile()
        dao.insert(FavouriteCountryEntity.fromCountry(profileId, country))
    }

    suspend fun remove(country: Country) {
        val profileId = profileRepository.ensureDefaultProfile()
        dao.delete(FavouriteCountryEntity.fromCountry(profileId, country))
    }

    suspend fun removeAll() {
        val profileId = profileRepository.ensureDefaultProfile()
        dao.deleteAllForProfile(profileId)
    }

    suspend fun togglePin(country: Country) {
        val profileId = profileRepository.ensureDefaultProfile()
        val existing = dao.getByCode(profileId, country.code) ?: return
        dao.updatePinned(profileId, country.code, !existing.isPinned)
    }

    suspend fun setTag(country: Country, tag: String?) {
        val profileId = profileRepository.ensureDefaultProfile()
        val existing = dao.getByCode(profileId, country.code) ?: return
        dao.updateTag(profileId, country.code, tag?.trim()?.ifBlank { null })
    }

    suspend fun isFavourite(countryCode: String): Boolean {
        val profileId = profileRepository.ensureDefaultProfile()
        return dao.getByCode(profileId, countryCode) != null
    }
}

package com.example.myapplication.data.repository

import com.example.myapplication.data.local.CollectionCountryCrossRef
import com.example.myapplication.data.local.CollectionsDao
import com.example.myapplication.data.local.CountryCollectionEntity
import com.example.myapplication.domain.model.Country
import com.example.myapplication.domain.model.CountryCollection
import com.example.myapplication.domain.repository.CountryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class CollectionsRepository @Inject constructor(
    private val dao: CollectionsDao,
    private val profileRepository: ProfileRepository,
    private val countryRepository: CountryRepository
) {

    fun observeCollections(): Flow<List<CountryCollection>> =
        profileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            dao.observeForProfile(profileId).map { list ->
                list.map { item ->
                    CountryCollection(item.id, item.name, item.createdAt, item.countryCount)
                }
            }
        }

    fun observeCollectionName(collectionId: Long): Flow<String> =
        dao.observeName(collectionId).map { it.orEmpty() }

    suspend fun createCollection(name: String): Long {
        val profileId = profileRepository.ensureDefaultProfile()
        return dao.insert(
            CountryCollectionEntity(
                profileId = profileId,
                name = name.trim(),
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteCollection(collectionId: Long) {
        dao.deleteCollectionWithCountries(collectionId)
    }

    suspend fun addCountryToCollection(collectionId: Long, countryCode: String) {
        dao.addCountry(CollectionCountryCrossRef(collectionId, countryCode))
    }

    suspend fun removeCountryFromCollection(collectionId: Long, countryCode: String) {
        dao.removeCountry(collectionId, countryCode)
    }

    fun observeCollectionCountries(collectionId: Long): Flow<List<Country>> =
        dao.observeCountryCodes(collectionId).flatMapLatest { codes ->
            countryRepository.observeCountries().map { all ->
                val byCode = all.associateBy { it.code }
                codes.mapNotNull { byCode[it] }
            }
        }

}

package com.example.myapplication.data.repository

import com.example.myapplication.data.local.BrowseHistoryDao
import com.example.myapplication.data.local.BrowseHistoryEntity
import com.example.myapplication.domain.model.BrowseHistoryEntry
import com.example.myapplication.domain.model.Country
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class HistoryRepository @Inject constructor(
    private val dao: BrowseHistoryDao,
    private val profileRepository: ProfileRepository
) {

    fun observeRecent(limit: Int = 50): Flow<List<BrowseHistoryEntry>> =
        profileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            dao.observeRecent(profileId, limit).map { list ->
                list.map { it.toDomain() }
            }
        }

    suspend fun recordView(country: Country) {
        val profileId = profileRepository.ensureDefaultProfile()
        dao.upsert(
            BrowseHistoryEntity(
                profileId = profileId,
                countryCode = country.code,
                countryName = country.name,
                countryFlag = country.flag,
                viewedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearHistory() {
        val profileId = profileRepository.ensureDefaultProfile()
        dao.clearForProfile(profileId)
    }

    suspend fun removeEntry(countryCode: String) {
        val profileId = profileRepository.ensureDefaultProfile()
        dao.deleteEntry(profileId, countryCode)
    }
}

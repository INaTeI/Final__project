package com.example.myapplication.data.repository

import androidx.room.withTransaction
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.BrowseHistoryDao
import com.example.myapplication.data.local.CollectionsDao
import com.example.myapplication.data.local.CountryNotesDao
import com.example.myapplication.data.local.FavouritesDao
import com.example.myapplication.data.local.UserProfileEntity
import com.example.myapplication.data.local.UserProfilesDao
import com.example.myapplication.domain.model.UserProfile
import com.example.myapplication.domain.preferences.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepository @Inject constructor(
    private val db: AppDatabase,
    private val dao: UserProfilesDao,
    private val favouritesDao: FavouritesDao,
    private val historyDao: BrowseHistoryDao,
    private val collectionsDao: CollectionsDao,
    private val notesDao: CountryNotesDao,
    private val preferences: AppPreferences
) {

    fun observeProfiles(): Flow<List<UserProfile>> =
        dao.observeAll().map { list -> list.map { it.toDomain() } }

    fun observeActiveProfileId(): Flow<Long> =
        preferences.observeActiveProfileId().map { id ->
            id ?: DEFAULT_PROFILE_ID
        }

    suspend fun ensureDefaultProfile(): Long {
        val profiles = dao.getAll()
        if (profiles.isEmpty()) {
            val id = dao.insert(
                UserProfileEntity(name = "Основной", createdAt = System.currentTimeMillis())
            )
            preferences.setActiveProfileId(id)
            return id
        }

        val activeId = preferences.getActiveProfileId()
        if (activeId == null || dao.getById(activeId) == null) {
            preferences.setActiveProfileId(profiles.first().id)
            return profiles.first().id
        }
        return activeId
    }

    suspend fun createProfile(name: String): Long {
        val id = dao.insert(
            UserProfileEntity(name = name.trim(), createdAt = System.currentTimeMillis())
        )
        preferences.setActiveProfileId(id)
        return id
    }

    suspend fun switchProfile(profileId: Long) {
        if (dao.getById(profileId) != null) {
            preferences.setActiveProfileId(profileId)
        }
    }

    suspend fun deleteProfile(profileId: Long) {
        val profiles = dao.getAll()
        if (profiles.size <= 1) return
        db.withTransaction {
            favouritesDao.deleteAllForProfile(profileId)
            notesDao.deleteAllForProfile(profileId)
            historyDao.clearForProfile(profileId)
            collectionsDao.deleteCountriesForProfileCollections(profileId)
            collectionsDao.deleteAllForProfile(profileId)
            dao.deleteById(profileId)
        }
        val activeId = preferences.getActiveProfileId()
        if (activeId == profileId) {
            val remaining = dao.getAll().firstOrNull() ?: return
            preferences.setActiveProfileId(remaining.id)
        }
    }

    companion object {
        const val DEFAULT_PROFILE_ID = 1L
    }
}

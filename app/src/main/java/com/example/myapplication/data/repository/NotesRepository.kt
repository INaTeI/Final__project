package com.example.myapplication.data.repository

import com.example.myapplication.data.local.CountryNoteEntity
import com.example.myapplication.data.local.CountryNotesDao
import com.example.myapplication.domain.model.CountryNote
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class NotesRepository @Inject constructor(
    private val dao: CountryNotesDao,
    private val profileRepository: ProfileRepository
) {

    fun observeNote(countryCode: String): Flow<CountryNote?> =
        profileRepository.observeActiveProfileId().flatMapLatest { profileId ->
            dao.observeNote(profileId, countryCode).map { entity ->
                entity?.toDomain()
            }
        }

    suspend fun saveNote(countryCode: String, text: String) {
        val profileId = profileRepository.ensureDefaultProfile()
        val trimmed = text.trim()
        if (trimmed.isEmpty()) {
            dao.delete(profileId, countryCode)
            return
        }
        dao.upsert(
            CountryNoteEntity(
                profileId = profileId,
                countryCode = countryCode,
                text = trimmed,
                updatedAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun deleteNote(countryCode: String) {
        val profileId = profileRepository.ensureDefaultProfile()
        dao.delete(profileId, countryCode)
    }
}

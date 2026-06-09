package com.example.myapplication.data

import com.example.myapplication.data.local.CountryNoteEntity
import com.example.myapplication.data.local.CountryNotesDao
import com.example.myapplication.data.repository.NotesRepository
import com.example.myapplication.data.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class NotesRepositoryTest {

    private val profileId = 1L
    private val notes = MutableStateFlow<CountryNoteEntity?>(null)

    private val dao = object : CountryNotesDao {
        override fun observeNote(profileId: Long, countryCode: String): Flow<CountryNoteEntity?> =
            notes

        override suspend fun upsert(note: CountryNoteEntity) {
            notes.value = note
        }

        override suspend fun delete(profileId: Long, countryCode: String) {
            notes.value = null
        }
    }

    private val profileRepository = mockk<ProfileRepository> {
        every { observeActiveProfileId() } returns kotlinx.coroutines.flow.flowOf(profileId)
        coEvery { ensureDefaultProfile() } returns profileId
    }

    private val repository = NotesRepository(dao, profileRepository)

    @Test
    fun saveNote_persistsText() = runTest {
        repository.saveNote("UA", "Моя заметка")

        val note = repository.observeNote("UA").first()
        assertEquals("Моя заметка", note?.text)
    }

    @Test
    fun saveNote_blankText_deletesNote() = runTest {
        repository.saveNote("UA", "Текст")
        repository.saveNote("UA", "   ")

        assertNull(repository.observeNote("UA").first())
    }
}

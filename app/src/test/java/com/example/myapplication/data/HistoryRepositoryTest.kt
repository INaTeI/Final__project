package com.example.myapplication.data

import com.example.myapplication.data.local.BrowseHistoryDao
import com.example.myapplication.data.local.BrowseHistoryEntity
import com.example.myapplication.data.repository.HistoryRepository
import com.example.myapplication.data.repository.ProfileRepository
import com.example.myapplication.fake.TestCountries
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryRepositoryTest {

    private val profileId = 1L
    private val entries = MutableStateFlow<List<BrowseHistoryEntity>>(emptyList())

    private val dao = object : BrowseHistoryDao {
        override fun observeRecent(profileId: Long, limit: Int): Flow<List<BrowseHistoryEntity>> =
            entries

        override suspend fun insert(entry: BrowseHistoryEntity) {
            entries.value = entries.value + entry
        }

        override suspend fun upsert(entry: BrowseHistoryEntity) {
            entries.value = entries.value
                .filterNot { it.profileId == entry.profileId && it.countryCode == entry.countryCode } + entry
        }

        override suspend fun clearForProfile(profileId: Long) {
            entries.value = emptyList()
        }

        override suspend fun deleteEntry(profileId: Long, countryCode: String) {
            entries.value = entries.value.filterNot { it.countryCode == countryCode }
        }
    }

    private val profileRepository = mockk<ProfileRepository> {
        every { observeActiveProfileId() } returns kotlinx.coroutines.flow.flowOf(profileId)
        coEvery { ensureDefaultProfile() } returns profileId
    }

    private val repository = HistoryRepository(dao, profileRepository)

    @Test
    fun recordView_addsEntryToHistory() = runTest {
        repository.recordView(TestCountries.ukraine)

        val history = repository.observeRecent().first()
        assertEquals(1, history.size)
        assertEquals(TestCountries.ukraine.code, history.first().countryCode)
    }

    @Test
    fun recordView_sameCountry_updatesTimestamp() = runTest {
        repository.recordView(TestCountries.ukraine)
        val firstView = repository.observeRecent().first().first().viewedAt

        repository.recordView(TestCountries.ukraine)
        val secondView = repository.observeRecent().first().first().viewedAt

        assertEquals(1, repository.observeRecent().first().size)
        assertTrue(secondView >= firstView)
    }


    @Test
    fun clearHistory_removesAllEntries() = runTest {
        repository.recordView(TestCountries.ukraine)
        repository.clearHistory()

        assertEquals(0, repository.observeRecent().first().size)
    }
}

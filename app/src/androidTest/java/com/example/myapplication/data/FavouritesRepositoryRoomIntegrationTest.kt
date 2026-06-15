package com.example.myapplication.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.repository.FavouritesRepository
import com.example.myapplication.data.repository.ProfileRepository
import com.example.myapplication.fake.TestCountries
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class FavouritesRepositoryRoomIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: FavouritesRepository
    private val profileId = 1L

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        val profileRepository = mockk<ProfileRepository>()
        every { profileRepository.observeActiveProfileId() } returns flowOf(profileId)
        coEvery { profileRepository.ensureDefaultProfile() } returns profileId

        repository = FavouritesRepository(database.favouritesDao(), profileRepository)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun duplicateInsert_doesNotCreateDuplicateRowsInRoom() = runTest {
        repository.add(TestCountries.ukraine)
        repository.add(TestCountries.ukraine)

        val stored = database.favouritesDao().getAllForProfile(profileId)
        assertEquals(1, stored.size)
        assertEquals("UA", stored.first().code)
    }
}

package com.example.myapplication.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.repository.CountryRepositoryImpl
import com.example.myapplication.domain.model.Country
import com.example.myapplication.domain.preferences.AppPreferences
import com.example.myapplication.fake.FakeCountriesApi
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CountryRepositoryImplIntegrationTest {

    private lateinit var database: AppDatabase
    private lateinit var repository: CountryRepositoryImpl

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        val preferences = mockk<AppPreferences>(relaxed = true)
        coEvery { preferences.setLastSyncTimestamp(any()) } returns Unit
        repository = CountryRepositoryImpl(FakeCountriesApi(), database.countriesDao(), preferences)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun refreshCountries_persistsDataInRoom_andCanBeReadAgain() = runTest {
        repository.refreshCountries()

        val fromDao = database.countriesDao().getAll()
        assertEquals(2, fromDao.size)

        val readAgain = database.countriesDao().getByCode("UA")
        assertEquals("Ukraine", readAgain?.name)
    }

    @Test
    fun observeCountries_emitsPersistedSequenceAfterRefresh() = runTest {
        repository.observeCountries().test {
            assertEquals(emptyList<Country>(), awaitItem())

            repository.refreshCountries()
            val firstLoaded = awaitItem()
            assertEquals(2, firstLoaded.size)
            assertEquals("UA", firstLoaded.first { it.code == "UA" }.code)

            val persisted = database.countriesDao().getAll()
            assertEquals(firstLoaded.map { it.code }.toSet(), persisted.map { it.code }.toSet())
            cancelAndIgnoreRemainingEvents()
        }
    }
}

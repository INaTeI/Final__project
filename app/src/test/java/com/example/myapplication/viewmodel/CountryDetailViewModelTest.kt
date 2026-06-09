package com.example.myapplication.viewmodel

import androidx.lifecycle.SavedStateHandle
import com.example.myapplication.data.repository.CollectionsRepository
import com.example.myapplication.data.repository.FavouritesRepository
import com.example.myapplication.data.repository.HistoryRepository
import com.example.myapplication.data.repository.NotesRepository
import com.example.myapplication.fake.FakeCountryRepository
import com.example.myapplication.fake.TestCountries
import com.example.myapplication.fake.createFakeFavouritesRepository
import com.example.myapplication.ui.state.CountriesRequestState
import com.example.myapplication.util.MainDispatcherRule
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountryDetailViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        repository: FakeCountryRepository,
        savedStateHandle: SavedStateHandle = SavedStateHandle()
    ): CountryDetailViewModel {
        val historyRepository = mockk<HistoryRepository>(relaxed = true)
        val notesRepository = mockk<NotesRepository>()
        every { notesRepository.observeNote(any()) } returns flowOf(null)
        val collectionsRepository = mockk<CollectionsRepository>()
        every { collectionsRepository.observeCollections() } returns flowOf(emptyList())
        val favouritesRepository = createFakeFavouritesRepository().first

        return CountryDetailViewModel(
            repository,
            historyRepository,
            notesRepository,
            favouritesRepository,
            collectionsRepository,
            savedStateHandle
        )
    }

    @Test
    fun loadCountry_success_setsLoadedWithCountry() = runTest {
        val repository = FakeCountryRepository().apply {
            setCountries(listOf(TestCountries.ukraine))
        }
        val viewModel = createViewModel(repository)
        viewModel.loadCountry("UA")
        advanceUntilIdle()

        assertEquals(CountriesRequestState.Loaded, viewModel.uiState.requestState)
        assertEquals(TestCountries.ukraine, viewModel.uiState.country)
    }

    @Test
    fun loadCountry_failure_setsError() = runTest {
        val repository = FakeCountryRepository()
        val viewModel = createViewModel(repository, SavedStateHandle(mapOf("code" to "XX")))
        viewModel.loadCountry("XX")
        advanceUntilIdle()

        assertTrue(viewModel.uiState.requestState is CountriesRequestState.Error)
    }

    @Test
    fun loadCountry_usesSavedStateHandle_whenNavArgBlank() = runTest {
        val repository = FakeCountryRepository().apply {
            setCountries(listOf(TestCountries.poland))
        }
        val viewModel = createViewModel(
            repository,
            SavedStateHandle(mapOf("code" to "PL"))
        )
        viewModel.loadCountry("")
        advanceUntilIdle()

        assertEquals(TestCountries.poland, viewModel.uiState.country)
    }

    @Test
    fun retry_afterFailure_triggersSecondLoadAttempt() = runTest {
        var getCountryCalls = 0
        val repository = FakeCountryRepository().apply {
            getCountryOverride = {
                getCountryCalls++
                if (getCountryCalls == 1) throw IllegalStateException("temporary failure")
                TestCountries.ukraine
            }
        }
        val viewModel = createViewModel(repository, SavedStateHandle(mapOf("code" to "UA")))
        viewModel.loadCountry("UA")
        advanceUntilIdle()
        assertTrue(viewModel.uiState.requestState is CountriesRequestState.Error)

        viewModel.retry()
        advanceUntilIdle()

        assertEquals(2, getCountryCalls)
        assertEquals(CountriesRequestState.Loaded, viewModel.uiState.requestState)
        assertEquals(TestCountries.ukraine, viewModel.uiState.country)
    }
}

package com.example.myapplication.viewmodel

import app.cash.turbine.test
import com.example.myapplication.domain.model.Country
import com.example.myapplication.domain.model.RegionFilter
import com.example.myapplication.fake.FakeCountryRepository
import com.example.myapplication.fake.FakeUserPreferencesRepository
import com.example.myapplication.fake.TestCountries
import com.example.myapplication.fake.createFakeFavouritesRepository
import com.example.myapplication.ui.state.CountriesRequestState
import com.example.myapplication.util.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CountriesListViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private fun createViewModel(
        countryRepository: FakeCountryRepository = FakeCountryRepository(),
        favouritesRepository: com.example.myapplication.data.repository.FavouritesRepository =
            createFakeFavouritesRepository().first,
        preferencesRepository: FakeUserPreferencesRepository = FakeUserPreferencesRepository()
    ): CountriesListViewModel {
        return CountriesListViewModel(
            countryRepository,
            favouritesRepository,
            preferencesRepository
        )
    }

    @Test
    fun initialLoad_withoutCacheAndNetworkError_setsError() = runTest {
        val repository = FakeCountryRepository().apply {
            shouldFailRefresh = true
        }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.requestState is CountriesRequestState.Error)
    }

    @Test
    fun loadCountries_success_setsLoadedState() = runTest {
        val repository = FakeCountryRepository().apply {
            setCountries(listOf(TestCountries.ukraine, TestCountries.poland))
            shouldFailRefresh = false
        }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        assertEquals(CountriesRequestState.Loaded, viewModel.uiState.value.requestState)
        assertEquals(2, viewModel.uiState.value.countries.size)
    }

    @Test
    fun loadCountries_failure_withoutCache_setsError() = runTest {
        val repository = FakeCountryRepository().apply {
            shouldFailRefresh = true
        }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        val state = viewModel.uiState.value.requestState
        assertTrue(state is CountriesRequestState.Error)
        assertEquals("Ошибка загрузки", (state as CountriesRequestState.Error).message)
    }

    @Test
    fun search_withNoMatches_setsEmptyState() = runTest {
        val repository = FakeCountryRepository().apply {
            setCountries(listOf(TestCountries.ukraine))
            shouldFailRefresh = false
        }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        viewModel.search("zzz")
        advanceTimeBy(400)
        advanceUntilIdle()

        assertEquals(CountriesRequestState.Empty, viewModel.uiState.value.requestState)
        assertTrue(viewModel.uiState.value.countries.isEmpty())
    }

    @Test
    fun retry_afterError_triggersNewRefreshAttempt() = runTest {
        val repository = FakeCountryRepository().apply {
            shouldFailRefresh = true
        }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()
        val callsAfterInit = repository.refreshCallCount

        repository.shouldFailRefresh = false
        repository.setCountries(listOf(TestCountries.ukraine))
        viewModel.loadCountries(forceRefresh = true)
        advanceUntilIdle()

        assertTrue(repository.refreshCallCount > callsAfterInit)
        assertEquals(CountriesRequestState.Loaded, viewModel.uiState.value.requestState)
    }

    @Test
    fun observeCountries_emitsFullSequenceFromRepository() = runTest {
        val repository = FakeCountryRepository().apply {
            setCountries(emptyList())
            shouldFailRefresh = false
        }
        repository.observeCountries().test {
            assertEquals(emptyList<Country>(), awaitItem())

            repository.emitCountries(listOf(TestCountries.ukraine))
            assertEquals(listOf(TestCountries.ukraine), awaitItem())

            repository.emitCountries(listOf(TestCountries.ukraine, TestCountries.poland))
            assertEquals(listOf(TestCountries.ukraine, TestCountries.poland), awaitItem())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun observeCountries_updatesViewModelWithoutDuplicateLoadedEmissions() = runTest {
        val repository = FakeCountryRepository().apply {
            setCountries(listOf(TestCountries.ukraine))
            shouldFailRefresh = false
        }
        val viewModel = createViewModel(repository)
        advanceUntilIdle()

        val countriesAfterFirstLoad = viewModel.uiState.value.countries
        repository.emitCountries(listOf(TestCountries.ukraine, TestCountries.japan))
        advanceUntilIdle()

        assertEquals(2, viewModel.uiState.value.countries.size)
        assertEquals(1, countriesAfterFirstLoad.size)
        assertEquals(CountriesRequestState.Loaded, viewModel.uiState.value.requestState)
    }

    @Test
    fun regionFilter_fromPreferences_filtersCountriesReactively() = runTest {
        val repository = FakeCountryRepository().apply {
            setCountries(listOf(TestCountries.ukraine, TestCountries.japan))
            shouldFailRefresh = false
        }
        val preferences = FakeUserPreferencesRepository(RegionFilter.ALL)
        val viewModel = createViewModel(repository, preferencesRepository = preferences)
        advanceUntilIdle()
        assertEquals(2, viewModel.uiState.value.countries.size)

        viewModel.setRegionFilter(RegionFilter.ASIA)
        advanceUntilIdle()

        assertEquals(RegionFilter.ASIA, viewModel.uiState.value.regionFilter)
        assertEquals(1, viewModel.uiState.value.countries.size)
        assertEquals(TestCountries.japan, viewModel.uiState.value.countries.first())
    }
}

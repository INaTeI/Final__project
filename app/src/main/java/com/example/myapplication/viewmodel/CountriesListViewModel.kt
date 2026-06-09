package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.sync.CachePolicy
import com.example.myapplication.domain.preferences.AppPreferences
import com.example.myapplication.data.repository.FavouritesRepository
import com.example.myapplication.domain.model.Country
import com.example.myapplication.domain.model.RegionFilter
import com.example.myapplication.domain.repository.CountryRepository
import com.example.myapplication.ui.state.CountriesListUiState
import com.example.myapplication.ui.state.CountriesRequestState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val SEARCH_DEBOUNCE_MS = 400L

private data class RefreshRequest(val force: Boolean = false)

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
@HiltViewModel
class CountriesListViewModel @Inject constructor(
    private val repository: CountryRepository,
    private val favouritesRepository: FavouritesRepository,
    private val preferences: AppPreferences
) : ViewModel() {

    private val searchQuery = MutableStateFlow("")

    private val refreshRequests = MutableSharedFlow<RefreshRequest>(
        extraBufferCapacity = 1
    )

    private val debouncedSearchQuery = searchQuery
        .debounce(SEARCH_DEBOUNCE_MS)
        .distinctUntilChanged()

    private val refreshState = refreshRequests
        .onStart { emit(RefreshRequest(force = false)) }
        .flatMapLatest { request ->
            flow {
                val ttl = preferences.observeCacheTtl().first()
                val lastSync = repository.getLastCacheTimestamp()
                val isStale = CachePolicy.isStale(lastSync, ttl)

                if (!request.force && repository.hasCachedCountries() && !isStale) {
                    emit(CountriesRequestState.Loaded)
                    return@flow
                }

                if (!request.force && repository.hasCachedCountries()) {
                    emit(CountriesRequestState.Loaded)
                    try {
                        repository.refreshCountries()
                    } catch (_: Exception) {
                    }
                    return@flow
                }

                emit(CountriesRequestState.Loading)
                try {
                    repository.refreshCountries()
                    emit(
                        if (repository.hasCachedCountries()) {
                            CountriesRequestState.Loaded
                        } else {
                            CountriesRequestState.Empty
                        }
                    )
                } catch (_: Exception) {
                    emit(CountriesRequestState.Error("Ошибка загрузки"))
                }
            }
        }

    val uiState: StateFlow<CountriesListUiState> = combine(
        combine(debouncedSearchQuery, preferences.observeRegionFilter(), repository.observeCountries(), favouritesRepository.observeFavouriteCodes(), refreshState) {
                query, regionFilter, allCountries, favouriteCodes, requestState ->
            listOf(query, regionFilter, allCountries, favouriteCodes, requestState)
        },
        preferences.observeCacheTtl(),
        preferences.observeLastSyncTimestamp()
    ) { inner, ttl, lastSync ->
        val query          = inner[0] as String
        val regionFilter   = inner[1] as RegionFilter
        @Suppress("UNCHECKED_CAST")
        val allCountries   = inner[2] as List<Country>
        @Suppress("UNCHECKED_CAST")
        val favouriteCodes = inner[3] as Set<String>
        val requestState   = inner[4] as CountriesRequestState

        val filtered = allCountries
            .asSequence()
            .filter { regionFilter.matches(it) }
            .filter { query.isBlank() || it.name.startsWith(query, ignoreCase = true) }
            .toList()

        val resolvedRequestState = when {
            requestState is CountriesRequestState.Error && allCountries.isNotEmpty() ->
                CountriesRequestState.Loaded
            requestState is CountriesRequestState.Loading && allCountries.isNotEmpty() ->
                CountriesRequestState.Loaded
            query.isNotBlank() && filtered.isEmpty() &&
                    requestState !is CountriesRequestState.Error ->
                CountriesRequestState.Empty
            allCountries.isEmpty() && requestState is CountriesRequestState.Loaded ->
                CountriesRequestState.Empty
            else -> requestState
        }

        CountriesListUiState(
            searchQuery = query,
            regionFilter = regionFilter,
            countries = filtered,
            favouriteCodes = favouriteCodes,
            requestState = resolvedRequestState,
            isCacheStale = CachePolicy.isStale(lastSync, ttl)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = CountriesListUiState()
    )

    fun search(query: String) {
        searchQuery.value = query
    }

    fun setRegionFilter(filter: RegionFilter) {
        viewModelScope.launch {
            preferences.setRegionFilter(filter)
        }
    }

    fun loadCountries(forceRefresh: Boolean = false) {
        refreshRequests.tryEmit(RefreshRequest(force = forceRefresh))
    }

    fun toggleFavourite(country: Country) {
        viewModelScope.launch {
            if (uiState.value.favouriteCodes.contains(country.code)) {
                favouritesRepository.remove(country)
            } else {
                favouritesRepository.add(country)
            }
        }
    }
}

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

private data class CountriesListSources(
    val filterQuery: String,
    val regionFilter: RegionFilter,
    val allCountries: List<Country>,
    val favouriteCodes: Set<String>,
    val requestState: CountriesRequestState
)

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
        searchQuery,
        combine(
            debouncedSearchQuery,
            preferences.observeRegionFilter(),
            repository.observeCountries(),
            favouritesRepository.observeFavouriteCodes(),
            refreshState
        ) { filterQuery, regionFilter, allCountries, favouriteCodes, requestState ->
            CountriesListSources(
                filterQuery = filterQuery,
                regionFilter = regionFilter,
                allCountries = allCountries,
                favouriteCodes = favouriteCodes,
                requestState = requestState
            )
        },
        preferences.observeCacheTtl(),
        preferences.observeLastSyncTimestamp()
    ) { currentQuery, sources, ttl, lastSync ->
        val filtered = sources.allCountries
            .asSequence()
            .filter { sources.regionFilter.matches(it) }
            .filter {
                sources.filterQuery.isBlank() ||
                        it.name.startsWith(sources.filterQuery, ignoreCase = true)
            }
            .toList()

        val resolvedRequestState = when {
            sources.requestState is CountriesRequestState.Error && sources.allCountries.isNotEmpty() ->
                CountriesRequestState.Loaded
            sources.requestState is CountriesRequestState.Loading && sources.allCountries.isNotEmpty() ->
                CountriesRequestState.Loaded
            sources.filterQuery.isNotBlank() && filtered.isEmpty() &&
                    sources.requestState !is CountriesRequestState.Error ->
                CountriesRequestState.Empty
            sources.allCountries.isEmpty() && sources.requestState is CountriesRequestState.Loaded ->
                CountriesRequestState.Empty
            else -> sources.requestState
        }

        CountriesListUiState(
            searchQuery = currentQuery,
            regionFilter = sources.regionFilter,
            countries = filtered,
            favouriteCodes = sources.favouriteCodes,
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

package com.example.myapplication.ui.state

import com.example.myapplication.domain.model.Country
import com.example.myapplication.domain.model.RegionFilter

data class CountriesListUiState(
    val searchQuery: String = "",
    val regionFilter: RegionFilter = RegionFilter.ALL,
    val countries: List<Country> = emptyList(),
    val favouriteCodes: Set<String> = emptySet(),
    val requestState: CountriesRequestState = CountriesRequestState.Loading,
    val isCacheStale: Boolean = false
)

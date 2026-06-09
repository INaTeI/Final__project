package com.example.myapplication.ui.state

import com.example.myapplication.domain.model.Country
import com.example.myapplication.domain.model.CountryCollection
import com.example.myapplication.domain.model.CountryNote

data class CountryDetailUiState(
    val country: Country? = null,
    val requestState: CountriesRequestState = CountriesRequestState.Loading,
    val note: CountryNote? = null,
    val noteDraft: String = "",
    val isFavourite: Boolean = false,
    val isPinned: Boolean = false,
    val userTag: String? = null,
    val collections: List<CountryCollection> = emptyList(),
    val showAddToCollectionDialog: Boolean = false
)

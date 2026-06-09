package com.example.myapplication.ui.state

import com.example.myapplication.domain.model.CountryCollection

data class CollectionsUiState(
    val collections: List<CountryCollection> = emptyList(),
    val newCollectionName: String = ""
)

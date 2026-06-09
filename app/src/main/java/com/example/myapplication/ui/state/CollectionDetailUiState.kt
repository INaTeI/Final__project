package com.example.myapplication.ui.state

import com.example.myapplication.domain.model.Country

data class CollectionDetailUiState(
    val collectionName: String = "",
    val countries: List<Country> = emptyList()
)

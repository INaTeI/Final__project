package com.example.myapplication.ui.state

import com.example.myapplication.domain.model.FavouriteCountry

data class FavouritesUiState(
    val favourites: List<FavouriteCountry> = emptyList()
)

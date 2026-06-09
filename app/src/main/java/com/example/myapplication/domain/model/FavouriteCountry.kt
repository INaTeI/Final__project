package com.example.myapplication.domain.model

data class FavouriteCountry(
    val country: Country,
    val isPinned: Boolean = false,
    val userTag: String? = null
)

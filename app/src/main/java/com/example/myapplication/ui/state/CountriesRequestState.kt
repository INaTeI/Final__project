package com.example.myapplication.ui.state

sealed class CountriesRequestState {
    object Loading : CountriesRequestState()
    object Loaded : CountriesRequestState()
    object Empty : CountriesRequestState()
    data class Error(val message: String) : CountriesRequestState()
}

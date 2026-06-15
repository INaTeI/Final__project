package com.example.myapplication.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.FavouritesRepository
import com.example.myapplication.domain.model.Country
import com.example.myapplication.ui.state.FavouritesUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class FavouritesViewModel @Inject constructor(
    private val favouritesRepository: FavouritesRepository
) : ViewModel() {

    var uiState by mutableStateOf(FavouritesUiState())
        private set

    init {
        viewModelScope.launch {
            favouritesRepository.observeFavourites().collect { favourites ->
                uiState = FavouritesUiState(favourites = favourites)
            }
        }
    }

    fun toggleFavourite(country: Country) {
        viewModelScope.launch {
            if (uiState.favourites.any { it.country.code == country.code }) {
                favouritesRepository.remove(country)
            } else {
                favouritesRepository.add(country)
            }
        }
    }

    fun togglePin(country: Country) {
        viewModelScope.launch {
            favouritesRepository.togglePin(country)
        }
    }

    fun setTag(country: Country, tag: String?) {
        viewModelScope.launch {
            favouritesRepository.setTag(country, tag)
        }
    }

    fun clearFavourites() {
        viewModelScope.launch {
            favouritesRepository.removeAll()
        }
    }
}

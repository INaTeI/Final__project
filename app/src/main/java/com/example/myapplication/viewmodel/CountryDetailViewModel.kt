package com.example.myapplication.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.CollectionsRepository
import com.example.myapplication.data.repository.FavouritesRepository
import com.example.myapplication.data.repository.HistoryRepository
import com.example.myapplication.data.repository.NotesRepository
import com.example.myapplication.domain.repository.CountryRepository
import com.example.myapplication.ui.state.CountriesRequestState
import com.example.myapplication.ui.state.CountryDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CountryDetailViewModel @Inject constructor(
    private val repository: CountryRepository,
    private val historyRepository: HistoryRepository,
    private val notesRepository: NotesRepository,
    private val favouritesRepository: FavouritesRepository,
    private val collectionsRepository: CollectionsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val savedCountryCode: String = savedStateHandle.get<String>("code").orEmpty()
    private var activeCode: String = ""

    var uiState by mutableStateOf(CountryDetailUiState())
        private set

    fun loadCountry(code: String) {
        val targetCode = code.ifBlank { savedCountryCode }
        if (targetCode.isBlank()) {
            uiState = uiState.copy(
                requestState = CountriesRequestState.Error("Страна не найдена")
            )
            return
        }

        activeCode = targetCode
        viewModelScope.launch {
            uiState = uiState.copy(requestState = CountriesRequestState.Loading)
            try {
                val country = repository.getCountry(targetCode)
                historyRepository.recordView(country)
                val isFav = favouritesRepository.isFavourite(targetCode)

                uiState = CountryDetailUiState(
                    country = country,
                    requestState = CountriesRequestState.Loaded,
                    isFavourite = isFav
                )

                launch {
                    notesRepository.observeNote(targetCode).collectLatest { note ->
                        uiState = uiState.copy(
                            note = note,
                            noteDraft = note?.text ?: uiState.noteDraft
                        )
                    }
                }

                launch {
                    favouritesRepository.observeFavourites().collectLatest { favourites ->
                        val fav = favourites.find { it.country.code == targetCode }
                        uiState = uiState.copy(
                            isFavourite = fav != null,
                            isPinned = fav?.isPinned == true,
                            userTag = fav?.userTag
                        )
                    }
                }

                launch {
                    collectionsRepository.observeCollections().collectLatest { collections ->
                        uiState = uiState.copy(collections = collections)
                    }
                }
            } catch (_: Exception) {
                uiState = uiState.copy(
                    requestState = CountriesRequestState.Error("Ошибка загрузки")
                )
            }
        }
    }

    fun retry() {
        loadCountry(activeCode)
    }

    fun updateNoteDraft(text: String) {
        uiState = uiState.copy(noteDraft = text)
    }

    fun saveNote() {
        viewModelScope.launch {
            notesRepository.saveNote(activeCode, uiState.noteDraft)
        }
    }

    fun toggleFavourite() {
        val country = uiState.country ?: return
        viewModelScope.launch {
            if (uiState.isFavourite) {
                favouritesRepository.remove(country)
            } else {
                favouritesRepository.add(country)
            }
        }
    }

    fun togglePin() {
        val country = uiState.country ?: return
        viewModelScope.launch {
            favouritesRepository.togglePin(country)
        }
    }

    fun setTag(tag: String?) {
        val country = uiState.country ?: return
        viewModelScope.launch {
            favouritesRepository.setTag(country, tag)
        }
    }

    fun showAddToCollectionDialog(show: Boolean) {
        uiState = uiState.copy(showAddToCollectionDialog = show)
    }

    fun addToCollection(collectionId: Long) {
        viewModelScope.launch {
            collectionsRepository.addCountryToCollection(collectionId, activeCode)
            uiState = uiState.copy(showAddToCollectionDialog = false)
        }
    }
}

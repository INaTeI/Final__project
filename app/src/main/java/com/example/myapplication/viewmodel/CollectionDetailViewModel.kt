package com.example.myapplication.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.CollectionsRepository
import com.example.myapplication.ui.state.CollectionDetailUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionDetailViewModel @Inject constructor(
    private val collectionsRepository: CollectionsRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val collectionId: Long =
        savedStateHandle.get<String>("collectionId")?.toLongOrNull() ?: 0L

    val uiState: StateFlow<CollectionDetailUiState> = combine(
        collectionsRepository.observeCollectionName(collectionId),
        collectionsRepository.observeCollectionCountries(collectionId)
    ) { name, countries ->
        CollectionDetailUiState(collectionName = name, countries = countries)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CollectionDetailUiState()
    )

    fun removeCountry(countryCode: String) {
        viewModelScope.launch {
            collectionsRepository.removeCountryFromCollection(collectionId, countryCode)
        }
    }
}

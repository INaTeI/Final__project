package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.CollectionsRepository
import com.example.myapplication.ui.state.CollectionsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CollectionsViewModel @Inject constructor(
    private val collectionsRepository: CollectionsRepository
) : ViewModel() {

    private val newCollectionName = MutableStateFlow("")

    val uiState: StateFlow<CollectionsUiState> = combine(
        collectionsRepository.observeCollections(),
        newCollectionName
    ) { collections, name ->
        CollectionsUiState(collections = collections, newCollectionName = name)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CollectionsUiState()
    )

    fun updateNewCollectionName(name: String) {
        newCollectionName.value = name
    }

    fun createCollection() {
        viewModelScope.launch {
            val name = newCollectionName.value.trim()
            if (name.isNotEmpty()) {
                collectionsRepository.createCollection(name)
                newCollectionName.value = ""
            }
        }
    }

    fun deleteCollection(collectionId: Long) {
        viewModelScope.launch { collectionsRepository.deleteCollection(collectionId) }
    }
}

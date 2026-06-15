package com.example.myapplication.ui.state

import com.example.myapplication.domain.model.BrowseHistoryEntry

data class HistoryUiState(
    val entries: List<BrowseHistoryEntry> = emptyList()
)

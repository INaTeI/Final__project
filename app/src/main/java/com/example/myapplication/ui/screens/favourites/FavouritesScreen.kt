package com.example.myapplication.ui.screens.favourites

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapplication.ui.components.CountryCard
import com.example.myapplication.viewmodel.FavouritesViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FavouritesScreen(
    vm: FavouritesViewModel,
    onCountryClick: (String) -> Unit,
    onBack: () -> Unit
) {
    val state = vm.uiState

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Избранное") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (state.favourites.isNotEmpty()) {
                        IconButton(onClick = { vm.clearFavourites() }) {
                            Icon(
                                imageVector = Icons.Default.Delete,
                                contentDescription = "Удалить все избранные"
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (state.favourites.isEmpty()) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text("Нет избранных стран")
            }
        } else {
            LazyColumn(
                Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                items(state.favourites, key = { it.country.code }) { favourite ->
                    CountryCard(
                        country = favourite.country,
                        isFavourite = true,
                        isPinned = favourite.isPinned,
                        userTag = favourite.userTag,
                        onClick = { onCountryClick(favourite.country.code) },
                        onFavourite = { vm.toggleFavourite(favourite.country) }
                    )
                }
            }
        }
    }
}

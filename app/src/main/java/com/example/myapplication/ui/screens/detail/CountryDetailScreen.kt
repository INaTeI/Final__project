package com.example.myapplication.ui.screens.detail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.example.myapplication.ui.TestTags
import com.example.myapplication.ui.state.CountriesRequestState
import com.example.myapplication.viewmodel.CountryDetailViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryDetailScreen(
    vm: CountryDetailViewModel,
    onBack: () -> Unit
) {
    val state = vm.uiState
    var tagDraft by remember(state.userTag) { mutableStateOf(state.userTag.orEmpty()) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "О стране",
                        modifier = Modifier.testTag(TestTags.DETAIL_TITLE)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = vm::toggleFavourite) {
                        Icon(
                            if (state.isFavourite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Избранное"
                        )
                    }
                    if (state.isFavourite) {
                        IconButton(onClick = vm::togglePin) {
                            Icon(
                                Icons.Default.PushPin,
                                contentDescription = "Закрепить",
                                tint = if (state.isPinned) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurface
                                }
                            )
                        }
                    }
                    IconButton(onClick = { vm.showAddToCollectionDialog(true) }) {
                        Icon(Icons.Default.CollectionsBookmark, contentDescription = "В коллекцию")
                    }
                }
            )
        }
    ) { paddingValues ->
        when (state.requestState) {
            CountriesRequestState.Loading -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            CountriesRequestState.Loaded -> {
                val country = state.country ?: return@Scaffold

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Image(
                        painter = rememberAsyncImagePainter(country.flag),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )

                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Text(country.name, style = MaterialTheme.typography.headlineSmall)
                            Text("Регион: ${country.region}")
                            Text("Столица: ${country.capital}")
                            Text("Население: ${country.population}")
                        }
                    }

                    if (state.isFavourite) {
                        OutlinedTextField(
                            value = tagDraft,
                            onValueChange = { tagDraft = it },
                            label = { Text("Пользовательский тег") },
                            modifier = Modifier.fillMaxWidth()
                        )
                        Button(onClick = { vm.setTag(tagDraft.ifBlank { null }) }) {
                            Text("Сохранить тег")
                        }
                    }

                    Text("Личная заметка", style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.noteDraft,
                        onValueChange = vm::updateNoteDraft,
                        label = { Text("Заметка о стране") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3
                    )
                    Button(onClick = vm::saveNote) {
                        Text("Сохранить заметку")
                    }
                    if (state.note != null) {
                        val formatted = java.text.SimpleDateFormat("dd.MM.yyyy HH:mm", java.util.Locale.getDefault())
                            .format(java.util.Date(state.note.updatedAt))
                        Text(
                            "Обновлено: $formatted",
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }

                if (state.showAddToCollectionDialog) {
                    AlertDialog(
                        onDismissRequest = { vm.showAddToCollectionDialog(false) },
                        title = { Text("Добавить в коллекцию") },
                        text = {
                            if (state.collections.isEmpty()) {
                                Text("Сначала создайте коллекцию на вкладке «Коллекции»")
                            } else {
                                Column {
                                    state.collections.forEach { collection ->
                                        TextButton(
                                            onClick = { vm.addToCollection(collection.id) },
                                            modifier = Modifier.fillMaxWidth()
                                        ) {
                                            Text("${collection.name} (${collection.countryCount})")
                                        }
                                    }
                                }
                            }
                        },
                        confirmButton = {
                            TextButton(onClick = { vm.showAddToCollectionDialog(false) }) {
                                Text("Закрыть")
                            }
                        }
                    )
                }
            }

            is CountriesRequestState.Error -> {
                Column(
                    Modifier.fillMaxSize().padding(paddingValues),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(state.requestState.message)
                    Button(onClick = { vm.retry() }) {
                        Text("Повторить")
                    }
                }
            }

            CountriesRequestState.Empty -> {}
        }
    }
}

package com.example.myapplication.ui.screens.list

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.myapplication.domain.model.RegionFilter
import com.example.myapplication.ui.TestTags
import com.example.myapplication.ui.components.CountryCard
import com.example.myapplication.ui.state.CountriesRequestState
import com.example.myapplication.viewmodel.CountriesListViewModel
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CountryListScreen(
    navController: NavController,
    vm: CountriesListViewModel
) {
    val state by vm.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Страны мира") },
                actions = {
                    IconButton(onClick = { navController.navigate("favourites") }) {
                        Icon(Icons.Default.Favorite, contentDescription = "Избранное")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (state.isCacheStale) {
                Text(
                    text = "Данные из кэша — обновление при наличии сети",
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.secondary
                )
            }

            TextField(
                value = state.searchQuery,
                onValueChange = vm::search,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .testTag(TestTags.SEARCH_FIELD),
                placeholder = { Text("Поиск страны") }
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RegionFilter.entries.forEach { filter ->
                    FilterChip(
                        selected = state.regionFilter == filter,
                        onClick = { vm.setRegionFilter(filter) },
                        label = { Text(filter.label) },
                        modifier = Modifier.testTag(TestTags.regionFilterChip(filter))
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            when (state.requestState) {
                CountriesRequestState.Loading -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }

                CountriesRequestState.Loaded -> {
                    SwipeRefresh(
                        state = rememberSwipeRefreshState(false),
                        onRefresh = { vm.loadCountries(forceRefresh = true) }
                    ) {
                        LazyColumn(modifier = Modifier.fillMaxSize()) {
                            items(state.countries) { country ->
                                CountryCard(
                                    country = country,
                                    isFavourite = state.favouriteCodes.contains(country.code),
                                    onClick = { navController.navigate("detail/${country.code}") },
                                    onFavourite = { vm.toggleFavourite(country) }
                                )
                            }
                        }
                    }
                }

                is CountriesRequestState.Error -> {
                    val message = (state.requestState as CountriesRequestState.Error).message
                    Column(
                        Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(message)
                        Button(
                            onClick = { vm.loadCountries(forceRefresh = true) },
                            modifier = Modifier.testTag(TestTags.RETRY_BUTTON)
                        ) {
                            Text("Повторить")
                        }
                    }
                }

                CountriesRequestState.Empty -> Box(
                    Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ничего не найдено",
                        modifier = Modifier.testTag(TestTags.EMPTY_MESSAGE)
                    )
                }
            }
        }
    }
}

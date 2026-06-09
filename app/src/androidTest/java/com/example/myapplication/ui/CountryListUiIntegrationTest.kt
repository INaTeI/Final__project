package com.example.myapplication.ui

import androidx.activity.ComponentActivity
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.myapplication.fake.FakeCountryRepository
import com.example.myapplication.fake.FakeUserPreferencesRepository
import com.example.myapplication.fake.TestCountries
import com.example.myapplication.fake.createTestCountryDetailDependencies
import com.example.myapplication.fake.createTestFavouritesRepository
import com.example.myapplication.ui.screens.detail.CountryDetailScreen
import com.example.myapplication.ui.screens.list.CountryListScreen
import com.example.myapplication.ui.state.CountriesRequestState
import com.example.myapplication.viewmodel.CountriesListViewModel
import com.example.myapplication.viewmodel.CountryDetailViewModel
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CountryListUiIntegrationTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun list_clickCountry_navigatesToDetailForCorrectId() {
        val countryRepository = FakeCountryRepository().apply {
            setCountries(listOf(TestCountries.ukraine, TestCountries.poland))
            shouldFailRefresh = false
        }
        val listViewModel = CountriesListViewModel(
            countryRepository,
            createTestFavouritesRepository(),
            FakeUserPreferencesRepository()
        )
        val detailDeps = createTestCountryDetailDependencies(countryRepository)
        var detailViewModel: CountryDetailViewModel? = null

        composeRule.setContent {
            val navController = rememberNavController()
            NavHost(navController, startDestination = "list") {
                composable("list") {
                    CountryListScreen(navController, listViewModel)
                }
                composable("detail/{code}") { backStackEntry ->
                    val code = backStackEntry.arguments?.getString("code").orEmpty()
                    val vm = remember(code) {
                        CountryDetailViewModel(
                            detailDeps.countryRepository,
                            detailDeps.historyRepository,
                            detailDeps.notesRepository,
                            detailDeps.favouritesRepository,
                            detailDeps.collectionsRepository,
                            SavedStateHandle(mapOf("code" to code))
                        ).also { detailViewModel = it }
                    }
                    LaunchedEffect(code) {
                        vm.loadCountry(code)
                    }
                    CountryDetailScreen(vm) { navController.popBackStack() }
                }
            }
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            listViewModel.uiState.value.requestState is CountriesRequestState.Loaded
        }

        composeRule.onNodeWithTag(TestTags.countryCard("UA")).performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            detailViewModel?.uiState?.requestState is CountriesRequestState.Loaded
        }
        composeRule.onNodeWithText("Ukraine").assertIsDisplayed()
    }

    @Test
    fun error_retry_showsLoadedState() {
        val countryRepository = FakeCountryRepository().apply {
            shouldFailRefresh = true
        }
        val listViewModel = CountriesListViewModel(
            countryRepository,
            createTestFavouritesRepository(),
            FakeUserPreferencesRepository()
        )

        composeRule.setContent {
            CountryListScreen(rememberNavController(), listViewModel)
        }

        composeRule.waitUntil(timeoutMillis = 10_000) {
            listViewModel.uiState.value.requestState is CountriesRequestState.Error
        }

        countryRepository.shouldFailRefresh = false
        countryRepository.setCountries(listOf(TestCountries.ukraine))

        composeRule.onNodeWithTag(TestTags.RETRY_BUTTON).performClick()

        composeRule.waitUntil(timeoutMillis = 10_000) {
            listViewModel.uiState.value.requestState is CountriesRequestState.Loaded
        }

        composeRule.onNodeWithTag(TestTags.countryCard("UA")).assertIsDisplayed()
    }
}

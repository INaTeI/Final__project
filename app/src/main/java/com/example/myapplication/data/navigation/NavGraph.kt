package com.example.myapplication.data.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.myapplication.domain.model.ThemeMode
import com.example.myapplication.domain.preferences.AppPreferences
import com.example.myapplication.ui.components.MainBottomBar
import com.example.myapplication.ui.components.MainDestination
import com.example.myapplication.ui.screens.collections.CollectionDetailScreen
import com.example.myapplication.ui.screens.collections.CollectionsScreen
import com.example.myapplication.ui.screens.detail.CountryDetailScreen
import com.example.myapplication.ui.screens.favourites.FavouritesScreen
import com.example.myapplication.ui.screens.history.HistoryScreen
import com.example.myapplication.ui.screens.list.CountryListScreen
import com.example.myapplication.ui.screens.settings.SettingsScreen
import com.example.myapplication.ui.theme.Android_dolg1Theme
import com.example.myapplication.viewmodel.CollectionDetailViewModel
import com.example.myapplication.viewmodel.CollectionsViewModel
import com.example.myapplication.viewmodel.CountriesListViewModel
import com.example.myapplication.viewmodel.CountryDetailViewModel
import com.example.myapplication.viewmodel.FavouritesViewModel
import com.example.myapplication.viewmodel.HistoryViewModel
import com.example.myapplication.viewmodel.SettingsViewModel
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@EntryPoint
@InstallIn(SingletonComponent::class)
interface PreferencesEntryPoint {
    fun appPreferences(): AppPreferences
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NavGraph() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route

    val context = androidx.compose.ui.platform.LocalContext.current
    val preferences = EntryPointAccessors.fromApplication(
        context.applicationContext,
        PreferencesEntryPoint::class.java
    ).appPreferences()

    val themeMode by preferences.observeThemeMode().collectAsStateWithLifecycle(ThemeMode.SYSTEM)
    val isSystemDark = androidx.compose.foundation.isSystemInDarkTheme()
    val darkTheme = themeMode.isDark ?: isSystemDark

    val showBottomBar = currentRoute in MainDestination.entries.map { it.route }

    Android_dolg1Theme(darkTheme = darkTheme, dynamicColor = false) {
        Scaffold(
            bottomBar = {
                if (showBottomBar) {
                    MainBottomBar(
                        currentRoute = currentRoute,
                        onNavigate = { destination ->
                            navController.navigate(destination.route) {
                                popUpTo(MainDestination.LIST.route) { saveState = true }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = MainDestination.LIST.route,
                modifier = Modifier.padding(padding)
            ) {
                composable(MainDestination.LIST.route) {
                    val vm: CountriesListViewModel = hiltViewModel()
                    CountryListScreen(navController, vm)
                }
                composable(MainDestination.COLLECTIONS.route) {
                    val vm: CollectionsViewModel = hiltViewModel()
                    CollectionsScreen(
                        vm = vm,
                        onCollectionClick = { id ->
                            navController.navigate("collection/$id")
                        }
                    )
                }
                composable(MainDestination.HISTORY.route) {
                    val vm: HistoryViewModel = hiltViewModel()
                    HistoryScreen(
                        vm = vm,
                        onCountryClick = { code -> navController.navigate("detail/$code") }
                    )
                }
                composable(MainDestination.SETTINGS.route) {
                    val vm: SettingsViewModel = hiltViewModel()
                    SettingsScreen(vm)
                }
                composable("detail/{code}") { backStackEntry ->
                    val code = backStackEntry.arguments?.getString("code").orEmpty()
                    val vm: CountryDetailViewModel = hiltViewModel()
                    LaunchedEffect(code) {
                        vm.loadCountry(code)
                    }
                    CountryDetailScreen(vm) { navController.popBackStack() }
                }
                composable("favourites") {
                    val vm: FavouritesViewModel = hiltViewModel()
                    FavouritesScreen(
                        vm = vm,
                        onCountryClick = { code -> navController.navigate("detail/$code") },
                        onBack = { navController.popBackStack() }
                    )
                }
                composable("collection/{collectionId}") { backStackEntry ->
                    val vm: CollectionDetailViewModel = hiltViewModel()
                    CollectionDetailScreen(
                        vm = vm,
                        onCountryClick = { code -> navController.navigate("detail/$code") },
                        onBack = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

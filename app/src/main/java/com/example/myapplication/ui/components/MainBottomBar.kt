package com.example.myapplication.ui.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.example.myapplication.R

enum class MainDestination(val route: String, val labelRes: Int) {
    LIST("list", R.string.nav_countries),
    COLLECTIONS("collections", R.string.nav_collections),
    HISTORY("history", R.string.nav_history),
    SETTINGS("settings", R.string.nav_settings)
}

@Composable
fun MainBottomBar(
    currentRoute: String?,
    onNavigate: (MainDestination) -> Unit
) {
    NavigationBar {
        MainDestination.entries.forEach { destination ->
            NavigationBarItem(
                selected = currentRoute == destination.route,
                onClick = { onNavigate(destination) },
                icon = {
                    Icon(
                        when (destination) {
                            MainDestination.LIST -> Icons.Default.Public
                            MainDestination.COLLECTIONS -> Icons.Default.CollectionsBookmark
                            MainDestination.HISTORY -> Icons.Default.History
                            MainDestination.SETTINGS -> Icons.Default.Settings
                        },
                        contentDescription = stringResource(destination.labelRes)
                    )
                },
                label = { Text(stringResource(destination.labelRes)) }
            )
        }
    }
}

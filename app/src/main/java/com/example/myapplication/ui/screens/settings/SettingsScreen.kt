package com.example.myapplication.ui.screens.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.myapplication.domain.model.CacheTtl
import com.example.myapplication.domain.model.ThemeMode
import com.example.myapplication.viewmodel.SettingsViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: SettingsViewModel) {
    val state by vm.uiState.collectAsStateWithLifecycle()
    var newProfileName by rememberSaveable { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(title = { Text("Настройки") })
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionTitle("Профили")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    state.profiles.forEach { profile ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            FilterChip(
                                selected = state.activeProfileId == profile.id,
                                onClick = { vm.switchProfile(profile.id) },
                                label = { Text(profile.name) }
                            )
                            if (state.profiles.size > 1) {
                                IconButton(onClick = { vm.deleteProfile(profile.id) }) {
                                    Icon(Icons.Default.Delete, contentDescription = "Удалить профиль")
                                }
                            }
                        }
                    }
                    OutlinedTextField(
                        value = newProfileName,
                        onValueChange = { newProfileName = it },
                        label = { Text("Новый профиль") },
                        modifier = Modifier.fillMaxWidth()
                    )
                    Button(
                        onClick = {
                            vm.createProfile(newProfileName)
                            newProfileName = ""
                        },
                        enabled = newProfileName.isNotBlank()
                    ) {
                        Text("Создать профиль")
                    }
                }
            }

            SectionTitle("Тема")
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ThemeMode.entries.forEach { mode ->
                    FilterChip(
                        selected = state.themeMode == mode,
                        onClick = { vm.setThemeMode(mode) },
                        label = { Text(mode.label) }
                    )
                }
            }

            HorizontalDivider()

            SectionTitle("Кэш и синхронизация")
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text("TTL кэша")
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        CacheTtl.entries.forEach { ttl ->
                            FilterChip(
                                selected = state.cacheTtl == ttl,
                                onClick = { vm.setCacheTtl(ttl) },
                                label = { Text(ttl.label) }
                            )
                        }
                    }

                    val syncText = if (state.lastSyncTimestamp > 0) {
                        val formatted = SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault())
                            .format(Date(state.lastSyncTimestamp))
                        "Последняя синхронизация: $formatted"
                    } else {
                        "Данные ещё не синхронизировались"
                    }
                    Text(syncText, style = MaterialTheme.typography.bodyMedium)
                    if (state.isCacheStale) {
                        Text(
                            "Кэш устарел — данные обновятся при наличии сети",
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("Фоновая синхронизация")
                        Switch(
                            checked = state.backgroundSyncEnabled,
                            onCheckedChange = vm::setBackgroundSyncEnabled
                        )
                    }

                    Button(onClick = vm::triggerSync) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Sync, contentDescription = null)
                            Spacer(Modifier.padding(horizontal = 4.dp))
                            Text("Обновить сейчас")
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(text: String) {
    Text(text, style = MaterialTheme.typography.titleMedium)
}

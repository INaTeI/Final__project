package com.example.myapplication.ui.state

import com.example.myapplication.domain.model.CacheTtl
import com.example.myapplication.domain.model.ThemeMode
import com.example.myapplication.domain.model.UserProfile

data class SettingsUiState(
    val profiles: List<UserProfile> = emptyList(),
    val activeProfileId: Long? = null,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val cacheTtl: CacheTtl = CacheTtl.HOURS_24,
    val backgroundSyncEnabled: Boolean = true,
    val lastSyncTimestamp: Long = 0L,
    val isCacheStale: Boolean = false,
    val newProfileName: String = ""
)

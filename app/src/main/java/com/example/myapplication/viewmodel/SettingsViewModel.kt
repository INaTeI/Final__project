package com.example.myapplication.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.data.repository.ProfileRepository
import com.example.myapplication.data.sync.CachePolicy
import com.example.myapplication.data.work.WorkManagerScheduler
import com.example.myapplication.domain.model.CacheTtl
import com.example.myapplication.domain.model.ThemeMode
import com.example.myapplication.domain.preferences.AppPreferences
import com.example.myapplication.ui.state.SettingsUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferences: AppPreferences,
    private val profileRepository: ProfileRepository,
    private val workManagerScheduler: WorkManagerScheduler
) : ViewModel() {

    val uiState: StateFlow<SettingsUiState> = combine(
        combine(
            profileRepository.observeProfiles(),
            preferences.observeActiveProfileId(),
            preferences.observeThemeMode(),
            preferences.observeCacheTtl(),
            preferences.observeBackgroundSyncEnabled()
        ) { profiles, activeId, theme, ttl, backgroundSync ->
            listOf(profiles, activeId, theme, ttl, backgroundSync)
        },
        preferences.observeLastSyncTimestamp()
    ) { inner, lastSync ->
        @Suppress("UNCHECKED_CAST")
        val profiles = inner[0] as List<com.example.myapplication.domain.model.UserProfile>
        val activeId = inner[1] as Long?
        val theme = inner[2] as ThemeMode
        val ttl = inner[3] as CacheTtl
        val backgroundSync = inner[4] as Boolean

        SettingsUiState(
            profiles = profiles,
            activeProfileId = activeId,
            themeMode = theme,
            cacheTtl = ttl,
            backgroundSyncEnabled = backgroundSync,
            lastSyncTimestamp = lastSync,
            isCacheStale = CachePolicy.isStale(lastSync, ttl)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = SettingsUiState()
    )

    private val newProfileName = kotlinx.coroutines.flow.MutableStateFlow("")

    fun updateNewProfileName(name: String) {
        newProfileName.value = name
    }

    fun setThemeMode(mode: ThemeMode) {
        viewModelScope.launch { preferences.setThemeMode(mode) }
    }

    fun setCacheTtl(ttl: CacheTtl) {
        viewModelScope.launch { preferences.setCacheTtl(ttl) }
    }

    fun setBackgroundSyncEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferences.setBackgroundSyncEnabled(enabled)
            workManagerScheduler.schedulePeriodicCacheRefresh(enabled)
        }
    }

    fun switchProfile(profileId: Long) {
        viewModelScope.launch { profileRepository.switchProfile(profileId) }
    }

    fun createProfile(name: String) {
        viewModelScope.launch {
            if (name.isNotBlank()) {
                profileRepository.createProfile(name)
                newProfileName.value = ""
            }
        }
    }

    fun deleteProfile(profileId: Long) {
        viewModelScope.launch { profileRepository.deleteProfile(profileId) }
    }

    fun triggerSync() {
        workManagerScheduler.triggerImmediateRefresh()
    }
}

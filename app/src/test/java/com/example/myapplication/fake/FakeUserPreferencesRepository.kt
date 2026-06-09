package com.example.myapplication.fake

import com.example.myapplication.domain.model.CacheTtl
import com.example.myapplication.domain.model.RegionFilter
import com.example.myapplication.domain.model.ThemeMode
import com.example.myapplication.domain.preferences.AppPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class FakeUserPreferencesRepository(
    initialFilter: RegionFilter = RegionFilter.ALL
) : AppPreferences {

    private val regionFilter = MutableStateFlow(initialFilter)
    private val activeProfileId = MutableStateFlow<Long?>(1L)
    private val themeMode = MutableStateFlow(ThemeMode.SYSTEM)
    private val cacheTtl = MutableStateFlow(CacheTtl.HOURS_24)
    private val backgroundSync = MutableStateFlow(true)
    private val lastSync = MutableStateFlow(0L)

    override fun observeRegionFilter(): Flow<RegionFilter> = regionFilter.asStateFlow()

    override suspend fun setRegionFilter(filter: RegionFilter) {
        regionFilter.value = filter
    }

    override fun observeActiveProfileId(): Flow<Long?> = activeProfileId.asStateFlow()

    override fun observeThemeMode(): Flow<ThemeMode> = themeMode.asStateFlow()

    override fun observeCacheTtl(): Flow<CacheTtl> = cacheTtl.asStateFlow()

    override fun observeBackgroundSyncEnabled(): Flow<Boolean> = backgroundSync.asStateFlow()

    override fun observeLastSyncTimestamp(): Flow<Long> = lastSync.asStateFlow()

    override suspend fun setActiveProfileId(profileId: Long) {
        activeProfileId.value = profileId
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        themeMode.value = mode
    }

    override suspend fun setCacheTtl(ttl: CacheTtl) {
        cacheTtl.value = ttl
    }

    override suspend fun setBackgroundSyncEnabled(enabled: Boolean) {
        backgroundSync.value = enabled
    }

    override suspend fun setLastSyncTimestamp(timestamp: Long) {
        lastSync.value = timestamp
    }

    override suspend fun getActiveProfileId(): Long? = activeProfileId.value

    override suspend fun getLastSyncTimestamp(): Long = lastSync.value
}

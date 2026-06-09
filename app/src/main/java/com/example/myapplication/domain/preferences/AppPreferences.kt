package com.example.myapplication.domain.preferences

import com.example.myapplication.domain.model.CacheTtl
import com.example.myapplication.domain.model.RegionFilter
import com.example.myapplication.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface AppPreferences : RegionFilterPreferences {
    fun observeActiveProfileId(): Flow<Long?>
    fun observeThemeMode(): Flow<ThemeMode>
    fun observeCacheTtl(): Flow<CacheTtl>
    fun observeBackgroundSyncEnabled(): Flow<Boolean>
    fun observeLastSyncTimestamp(): Flow<Long>

    suspend fun setActiveProfileId(profileId: Long)
    suspend fun setThemeMode(mode: ThemeMode)
    suspend fun setCacheTtl(ttl: CacheTtl)
    suspend fun setBackgroundSyncEnabled(enabled: Boolean)
    suspend fun setLastSyncTimestamp(timestamp: Long)

    suspend fun getActiveProfileId(): Long?
    suspend fun getLastSyncTimestamp(): Long
}

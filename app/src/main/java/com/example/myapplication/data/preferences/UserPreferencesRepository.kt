package com.example.myapplication.data.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.myapplication.domain.model.CacheTtl
import com.example.myapplication.domain.model.RegionFilter
import com.example.myapplication.domain.model.ThemeMode
import com.example.myapplication.domain.preferences.AppPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.userPreferencesDataStore: DataStore<Preferences> by preferencesDataStore(
    name = "user_preferences"
)

@Singleton
class UserPreferencesRepository @Inject constructor(
    @ApplicationContext private val context: Context
) : AppPreferences {
    private val dataStore = context.userPreferencesDataStore

    override fun observeRegionFilter(): Flow<RegionFilter> =
        dataStore.data.map { prefs ->
            RegionFilter.fromKey(prefs[REGION_FILTER_KEY].orEmpty())
        }

    override suspend fun setRegionFilter(filter: RegionFilter) {
        dataStore.edit { prefs ->
            prefs[REGION_FILTER_KEY] = filter.name
        }
    }

    override fun observeActiveProfileId(): Flow<Long?> =
        dataStore.data.map { prefs -> prefs[ACTIVE_PROFILE_ID_KEY] }

    override fun observeThemeMode(): Flow<ThemeMode> =
        dataStore.data.map { prefs ->
            ThemeMode.fromKey(prefs[THEME_MODE_KEY].orEmpty())
        }

    override fun observeCacheTtl(): Flow<CacheTtl> =
        dataStore.data.map { prefs ->
            CacheTtl.fromHours(prefs[CACHE_TTL_HOURS_KEY]?.toInt() ?: CacheTtl.HOURS_24.hours)
        }

    override fun observeBackgroundSyncEnabled(): Flow<Boolean> =
        dataStore.data.map { prefs ->
            prefs[BACKGROUND_SYNC_KEY] ?: true
        }

    override fun observeLastSyncTimestamp(): Flow<Long> =
        dataStore.data.map { prefs ->
            prefs[LAST_SYNC_TIMESTAMP_KEY] ?: 0L
        }

    override suspend fun setActiveProfileId(profileId: Long) {
        dataStore.edit { prefs ->
            prefs[ACTIVE_PROFILE_ID_KEY] = profileId
        }
    }

    override suspend fun setThemeMode(mode: ThemeMode) {
        dataStore.edit { prefs ->
            prefs[THEME_MODE_KEY] = mode.name
        }
    }

    override suspend fun setCacheTtl(ttl: CacheTtl) {
        dataStore.edit { prefs ->
            prefs[CACHE_TTL_HOURS_KEY] = ttl.hours.toLong()
        }
    }

    override suspend fun setBackgroundSyncEnabled(enabled: Boolean) {
        dataStore.edit { prefs ->
            prefs[BACKGROUND_SYNC_KEY] = enabled
        }
    }

    override suspend fun setLastSyncTimestamp(timestamp: Long) {
        dataStore.edit { prefs ->
            prefs[LAST_SYNC_TIMESTAMP_KEY] = timestamp
        }
    }

    override suspend fun getActiveProfileId(): Long? =
        dataStore.data.first()[ACTIVE_PROFILE_ID_KEY]

    override suspend fun getLastSyncTimestamp(): Long =
        dataStore.data.first()[LAST_SYNC_TIMESTAMP_KEY] ?: 0L

    companion object {
        private val REGION_FILTER_KEY = stringPreferencesKey("region_filter")
        private val ACTIVE_PROFILE_ID_KEY = longPreferencesKey("active_profile_id")
        private val THEME_MODE_KEY = stringPreferencesKey("theme_mode")
        private val CACHE_TTL_HOURS_KEY = longPreferencesKey("cache_ttl_hours")
        private val BACKGROUND_SYNC_KEY = booleanPreferencesKey("background_sync_enabled")
        private val LAST_SYNC_TIMESTAMP_KEY = longPreferencesKey("last_sync_timestamp")
    }
}

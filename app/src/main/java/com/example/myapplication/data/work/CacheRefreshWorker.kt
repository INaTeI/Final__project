package com.example.myapplication.data.work

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.myapplication.data.sync.CachePolicy
import com.example.myapplication.domain.preferences.AppPreferences
import com.example.myapplication.domain.repository.CountryRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first

@HiltWorker
class CacheRefreshWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val countryRepository: CountryRepository,
    private val preferences: AppPreferences
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val backgroundSyncEnabled = preferences.observeBackgroundSyncEnabled().first()
        if (!backgroundSyncEnabled) return Result.success()

        val ttl = preferences.observeCacheTtl().first()
        val lastSync = countryRepository.getLastCacheTimestamp()
        if (!CachePolicy.isStale(lastSync, ttl)) {
            return Result.success()
        }

        return try {
            countryRepository.refreshCountries()
            Result.success()
        } catch (_: Exception) {
            if (countryRepository.hasCachedCountries()) {
                Result.success()
            } else {
                Result.retry()
            }
        }
    }
}

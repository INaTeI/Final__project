package com.example.myapplication.data.work

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class WorkManagerScheduler @Inject constructor(
    @dagger.hilt.android.qualifiers.ApplicationContext private val context: Context
) {
    fun scheduleOfflinePreload() {
        val request = OneTimeWorkRequestBuilder<OfflinePreloadWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            OFFLINE_PRELOAD_WORK,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun schedulePeriodicCacheRefresh(enabled: Boolean) {
        if (!enabled) {
            WorkManager.getInstance(context).cancelUniqueWork(PERIODIC_CACHE_REFRESH_WORK)
            return
        }

        val request = PeriodicWorkRequestBuilder<CacheRefreshWorker>(6, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            PERIODIC_CACHE_REFRESH_WORK,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    fun triggerImmediateRefresh() {
        val request = OneTimeWorkRequestBuilder<CacheRefreshWorker>()
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            IMMEDIATE_REFRESH_WORK,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    companion object {
        const val OFFLINE_PRELOAD_WORK = "offline_preload"
        const val PERIODIC_CACHE_REFRESH_WORK = "periodic_cache_refresh"
        const val IMMEDIATE_REFRESH_WORK = "immediate_cache_refresh"
    }
}
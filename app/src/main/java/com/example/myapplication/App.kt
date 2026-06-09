package com.example.myapplication

import android.app.Application
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import com.example.myapplication.data.repository.ProfileRepository
import com.example.myapplication.data.work.WorkManagerScheduler
import com.example.myapplication.domain.preferences.AppPreferences
import dagger.hilt.android.HiltAndroidApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltAndroidApp
class App : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory
    @Inject lateinit var profileRepository: ProfileRepository
    @Inject lateinit var workManagerScheduler: WorkManagerScheduler
    @Inject lateinit var preferences: AppPreferences

    private val appScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()

    override fun onCreate() {
        super.onCreate()
        appScope.launch {
            profileRepository.ensureDefaultProfile()
            workManagerScheduler.scheduleOfflinePreload()
            val backgroundSync = preferences.observeBackgroundSyncEnabled().first()
            workManagerScheduler.schedulePeriodicCacheRefresh(backgroundSync)
        }
    }
}

package com.example.myapplication.data.work

import android.content.Context
import android.util.Log
import androidx.test.core.app.ApplicationProvider
import androidx.work.Configuration
import androidx.work.CoroutineWorker
import androidx.work.ListenableWorker
import androidx.work.WorkInfo
import androidx.work.WorkManager
import androidx.work.WorkerFactory
import androidx.work.WorkerParameters
import androidx.work.impl.utils.SynchronousExecutor
import androidx.work.testing.TestListenableWorkerBuilder
import androidx.work.testing.WorkManagerTestInitHelper
import com.example.myapplication.domain.model.CacheTtl
import com.example.myapplication.domain.model.Country
import com.example.myapplication.domain.model.RegionFilter
import com.example.myapplication.domain.model.ThemeMode
import com.example.myapplication.domain.preferences.AppPreferences
import com.example.myapplication.domain.repository.CountryRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.IOException

class WorkersAndSchedulerTest {

    private lateinit var context: Context

    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        WorkManagerTestInitHelper.initializeTestWorkManager(
            context,
            Configuration.Builder()
                .setMinimumLoggingLevel(Log.DEBUG)
                .setExecutor(SynchronousExecutor())
                .setWorkerFactory(NoOpWorkerFactory())
                .build()
        )
    }

    @Test
    fun offlinePreload_refreshesWhenCacheIsEmpty() = runTest {
        val repository = FakeWorkerCountryRepository(hasCache = false)
        val worker = TestListenableWorkerBuilder<OfflinePreloadWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ) = OfflinePreloadWorker(appContext, workerParameters, repository)
            })
            .build()

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertEquals(1, repository.refreshCallCount)
    }

    @Test
    fun offlinePreload_retriesWhenInitialRefreshFails() = runTest {
        val repository = FakeWorkerCountryRepository(
            hasCache = false,
            failRefresh = true
        )
        val worker = TestListenableWorkerBuilder<OfflinePreloadWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ) = OfflinePreloadWorker(appContext, workerParameters, repository)
            })
            .build()

        assertEquals(ListenableWorker.Result.retry(), worker.doWork())
        assertEquals(1, repository.refreshCallCount)
    }

    @Test
    fun cacheRefresh_skipsWorkWhenBackgroundSyncDisabled() = runTest {
        val repository = FakeWorkerCountryRepository(hasCache = true)
        val preferences = FakeWorkerPreferences(backgroundSyncEnabled = false)
        val worker = TestListenableWorkerBuilder<CacheRefreshWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ) = CacheRefreshWorker(appContext, workerParameters, repository, preferences)
            })
            .build()

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertEquals(0, repository.refreshCallCount)
    }

    @Test
    fun cacheRefresh_refreshesWhenCacheIsStale() = runTest {
        val repository = FakeWorkerCountryRepository(
            hasCache = true,
            lastCacheTimestamp = 1L
        )
        val preferences = FakeWorkerPreferences(cacheTtl = CacheTtl.HOURS_6)
        val worker = TestListenableWorkerBuilder<CacheRefreshWorker>(context)
            .setWorkerFactory(object : WorkerFactory() {
                override fun createWorker(
                    appContext: Context,
                    workerClassName: String,
                    workerParameters: WorkerParameters
                ) = CacheRefreshWorker(appContext, workerParameters, repository, preferences)
            })
            .build()

        assertEquals(ListenableWorker.Result.success(), worker.doWork())
        assertEquals(1, repository.refreshCallCount)
    }

    @Test
    fun scheduler_enqueuesUniqueOfflinePreloadWork() {
        val scheduler = WorkManagerScheduler(context)

        scheduler.scheduleOfflinePreload()

        val workInfos = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WorkManagerScheduler.OFFLINE_PRELOAD_WORK)
            .get()
        assertEquals(1, workInfos.size)
        assertTrue(workInfos.first().state in listOf(WorkInfo.State.ENQUEUED, WorkInfo.State.SUCCEEDED))
    }

    @Test
    fun scheduler_enqueuesAndCancelsPeriodicCacheRefresh() {
        val scheduler = WorkManagerScheduler(context)

        scheduler.schedulePeriodicCacheRefresh(enabled = true)
        val scheduled = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WorkManagerScheduler.PERIODIC_CACHE_REFRESH_WORK)
            .get()
        assertEquals(1, scheduled.size)

        scheduler.schedulePeriodicCacheRefresh(enabled = false)
        val afterCancel = WorkManager.getInstance(context)
            .getWorkInfosForUniqueWork(WorkManagerScheduler.PERIODIC_CACHE_REFRESH_WORK)
            .get()
        assertTrue(afterCancel.all { it.state == WorkInfo.State.CANCELLED })
    }

    private class NoOpWorkerFactory : WorkerFactory() {
        override fun createWorker(
            appContext: Context,
            workerClassName: String,
            workerParameters: WorkerParameters
        ): ListenableWorker = object : CoroutineWorker(appContext, workerParameters) {
            override suspend fun doWork(): Result = Result.success()
        }
    }

    private class FakeWorkerCountryRepository(
        private val hasCache: Boolean,
        private val failRefresh: Boolean = false,
        private val lastCacheTimestamp: Long = 0L
    ) : CountryRepository {
        var refreshCallCount = 0
            private set

        override fun observeCountries(): Flow<List<Country>> = MutableStateFlow(emptyList()).asStateFlow()

        override suspend fun refreshCountries() {
            refreshCallCount++
            if (failRefresh) throw IOException("refresh failed")
        }

        override suspend fun getCountry(code: String): Country {
            error("Not needed in worker tests")
        }

        override suspend fun hasCachedCountries(): Boolean = hasCache

        override suspend fun getLastCacheTimestamp(): Long = lastCacheTimestamp
    }

    private class FakeWorkerPreferences(
        private val backgroundSyncEnabled: Boolean = true,
        private val cacheTtl: CacheTtl = CacheTtl.HOURS_24
    ) : AppPreferences {
        override fun observeRegionFilter(): Flow<RegionFilter> =
            MutableStateFlow(RegionFilter.ALL).asStateFlow()

        override fun observeActiveProfileId(): Flow<Long?> = MutableStateFlow(1L).asStateFlow()

        override fun observeThemeMode(): Flow<ThemeMode> = MutableStateFlow(ThemeMode.SYSTEM).asStateFlow()

        override fun observeCacheTtl(): Flow<CacheTtl> = MutableStateFlow(cacheTtl).asStateFlow()

        override fun observeBackgroundSyncEnabled(): Flow<Boolean> =
            MutableStateFlow(backgroundSyncEnabled).asStateFlow()

        override fun observeLastSyncTimestamp(): Flow<Long> = MutableStateFlow(0L).asStateFlow()

        override suspend fun setRegionFilter(filter: RegionFilter) = Unit

        override suspend fun setActiveProfileId(profileId: Long) = Unit

        override suspend fun setThemeMode(mode: ThemeMode) = Unit

        override suspend fun setCacheTtl(ttl: CacheTtl) = Unit

        override suspend fun setBackgroundSyncEnabled(enabled: Boolean) = Unit

        override suspend fun setLastSyncTimestamp(timestamp: Long) = Unit

        override suspend fun getActiveProfileId(): Long? = 1L

        override suspend fun getLastSyncTimestamp(): Long = 0L
    }
}

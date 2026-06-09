package com.example.myapplication.data.sync

import com.example.myapplication.domain.model.CacheTtl

object CachePolicy {

    fun isStale(lastSyncTimestamp: Long, ttl: CacheTtl, now: Long = System.currentTimeMillis()): Boolean {
        if (lastSyncTimestamp <= 0L) return true
        val ttlMillis = ttl.hours * 60L * 60L * 1000L
        return now - lastSyncTimestamp >= ttlMillis
    }

    fun remainingTtlMillis(lastSyncTimestamp: Long, ttl: CacheTtl, now: Long = System.currentTimeMillis()): Long {
        if (lastSyncTimestamp <= 0L) return 0L
        val ttlMillis = ttl.hours * 60L * 60L * 1000L
        return (ttlMillis - (now - lastSyncTimestamp)).coerceAtLeast(0L)
    }
}

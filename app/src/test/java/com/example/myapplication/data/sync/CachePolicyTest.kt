package com.example.myapplication.data.sync

import com.example.myapplication.domain.model.CacheTtl
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class CachePolicyTest {

    @Test
    fun isStale_returnsTrue_whenNeverSynced() {
        assertTrue(CachePolicy.isStale(0L, CacheTtl.HOURS_24))
    }

    @Test
    fun isStale_returnsFalse_whenWithinTtl() {
        val now = 1_000_000L
        val lastSync = now - 60 * 60 * 1000L
        assertFalse(CachePolicy.isStale(lastSync, CacheTtl.HOURS_24, now))
    }

    @Test
    fun isStale_returnsTrue_whenTtlExpired() {
        val now = 1_000_000L
        val lastSync = now - 25 * 60 * 60 * 1000L
        assertTrue(CachePolicy.isStale(lastSync, CacheTtl.HOURS_24, now))
    }
}

package com.example.myapplication.domain.preferences

import com.example.myapplication.domain.model.RegionFilter
import kotlinx.coroutines.flow.Flow

interface RegionFilterPreferences {
    fun observeRegionFilter(): Flow<RegionFilter>
    suspend fun setRegionFilter(filter: RegionFilter)
}

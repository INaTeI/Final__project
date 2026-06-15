package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.BrowseHistoryEntry

@Entity(
    tableName = "browse_history",
    indices = [Index(value = ["profileId", "countryCode"], unique = true)]
)
data class BrowseHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val countryCode: String,
    val countryName: String,
    val countryFlag: String,
    val viewedAt: Long
) {
    fun toDomain() = BrowseHistoryEntry(id, countryCode, countryName, countryFlag, viewedAt)
}

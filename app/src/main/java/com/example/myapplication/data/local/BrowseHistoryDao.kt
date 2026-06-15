package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface BrowseHistoryDao {

    @Query(
        """
        SELECT * FROM browse_history
        WHERE profileId = :profileId
        ORDER BY viewedAt DESC
        LIMIT :limit
        """
    )
    fun observeRecent(profileId: Long, limit: Int = 50): Flow<List<BrowseHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entry: BrowseHistoryEntity)

    @Transaction
    suspend fun upsert(entry: BrowseHistoryEntity) {
        deleteEntry(entry.profileId, entry.countryCode)
        insert(entry)
    }

    @Query("DELETE FROM browse_history WHERE profileId = :profileId")
    suspend fun clearForProfile(profileId: Long)

    @Query("DELETE FROM browse_history WHERE profileId = :profileId AND countryCode = :countryCode")
    suspend fun deleteEntry(profileId: Long, countryCode: String)
}

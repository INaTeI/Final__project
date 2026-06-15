package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavouritesDao {

    @Query(
        """
        SELECT * FROM favourites
        WHERE profileId = :profileId
        ORDER BY isPinned DESC, name ASC
        """
    )
    fun observeForProfile(profileId: Long): Flow<List<FavouriteCountryEntity>>

    @Query("SELECT * FROM favourites WHERE profileId = :profileId")
    suspend fun getAllForProfile(profileId: Long): List<FavouriteCountryEntity>

    @Query("SELECT * FROM favourites WHERE profileId = :profileId AND code = :code LIMIT 1")
    suspend fun getByCode(profileId: Long, code: String): FavouriteCountryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(country: FavouriteCountryEntity)

    @Delete
    suspend fun delete(country: FavouriteCountryEntity)

    @Query("DELETE FROM favourites WHERE profileId = :profileId")
    suspend fun deleteAllForProfile(profileId: Long)

    @Query(
        """
        UPDATE favourites SET isPinned = :isPinned
        WHERE profileId = :profileId AND code = :code
        """
    )
    suspend fun updatePinned(profileId: Long, code: String, isPinned: Boolean)

    @Query(
        """
        UPDATE favourites SET userTag = :userTag
        WHERE profileId = :profileId AND code = :code
        """
    )
    suspend fun updateTag(profileId: Long, code: String, userTag: String?)
}

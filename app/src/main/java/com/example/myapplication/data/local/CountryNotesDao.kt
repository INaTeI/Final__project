package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CountryNotesDao {

    @Query(
        """
        SELECT * FROM country_notes
        WHERE profileId = :profileId AND countryCode = :countryCode
        LIMIT 1
        """
    )
    fun observeNote(profileId: Long, countryCode: String): Flow<CountryNoteEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(note: CountryNoteEntity)

    @Query("DELETE FROM country_notes WHERE profileId = :profileId AND countryCode = :countryCode")
    suspend fun delete(profileId: Long, countryCode: String)

    @Query("DELETE FROM country_notes WHERE profileId = :profileId")
    suspend fun deleteAllForProfile(profileId: Long)
}

package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow

data class CollectionWithCount(
    val id: Long,
    val profileId: Long,
    val name: String,
    val createdAt: Long,
    val countryCount: Int
)

@Dao
interface CollectionsDao {

    @Query(
        """
        SELECT c.id, c.profileId, c.name, c.createdAt,
               COUNT(cc.countryCode) AS countryCount
        FROM collections c
        LEFT JOIN collection_countries cc ON c.id = cc.collectionId
        WHERE c.profileId = :profileId
        GROUP BY c.id
        ORDER BY c.createdAt DESC
        """
    )
    fun observeForProfile(profileId: Long): Flow<List<CollectionWithCount>>

    @Query("SELECT * FROM collections WHERE id = :id LIMIT 1")
    suspend fun getById(id: Long): CountryCollectionEntity?

    @Query("SELECT name FROM collections WHERE id = :id LIMIT 1")
    fun observeName(id: Long): Flow<String?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(collection: CountryCollectionEntity): Long

    @Query("DELETE FROM collections WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun addCountry(ref: CollectionCountryCrossRef)

    @Query("DELETE FROM collection_countries WHERE collectionId = :collectionId AND countryCode = :countryCode")
    suspend fun removeCountry(collectionId: Long, countryCode: String)

    @Query(
        """
        SELECT countryCode FROM collection_countries
        WHERE collectionId = :collectionId
        ORDER BY countryCode
        """
    )
    fun observeCountryCodes(collectionId: Long): Flow<List<String>>

    @Transaction
    suspend fun deleteCollectionWithCountries(collectionId: Long) {
        deleteCountriesOfCollection(collectionId)
        deleteById(collectionId)
    }

    @Query("DELETE FROM collection_countries WHERE collectionId = :collectionId")
    suspend fun deleteCountriesOfCollection(collectionId: Long)
}

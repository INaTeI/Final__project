package com.example.myapplication.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface CountriesDao {

    @Query("SELECT * FROM countries ORDER BY name")
    fun observeAll(): Flow<List<CountryEntity>>

    @Query("SELECT * FROM countries ORDER BY name")
    suspend fun getAll(): List<CountryEntity>

    @Query("SELECT * FROM countries WHERE code = :code LIMIT 1")
    suspend fun getByCode(code: String): CountryEntity?

    @Query("SELECT MAX(cachedAt) FROM countries")
    suspend fun getLastCacheTimestamp(): Long?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(countries: List<CountryEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(country: CountryEntity)
}

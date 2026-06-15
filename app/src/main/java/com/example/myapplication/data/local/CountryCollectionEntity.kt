package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.CountryCollection

@Entity(tableName = "collections")
data class CountryCollectionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val profileId: Long,
    val name: String,
    val createdAt: Long
) {
    fun toDomain(countryCount: Int = 0) =
        CountryCollection(id, name, createdAt, countryCount)
}

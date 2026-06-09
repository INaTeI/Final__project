package com.example.myapplication.data.local

import androidx.room.Entity

@Entity(
    tableName = "collection_countries",
    primaryKeys = ["collectionId", "countryCode"]
)
data class CollectionCountryCrossRef(
    val collectionId: Long,
    val countryCode: String
)

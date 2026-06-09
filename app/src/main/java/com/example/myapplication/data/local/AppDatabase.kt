package com.example.myapplication.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(
    entities = [
        CountryEntity::class,
        FavouriteCountryEntity::class,
        UserProfileEntity::class,
        BrowseHistoryEntity::class,
        CountryCollectionEntity::class,
        CollectionCountryCrossRef::class,
        CountryNoteEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun countriesDao(): CountriesDao
    abstract fun favouritesDao(): FavouritesDao
    abstract fun userProfilesDao(): UserProfilesDao
    abstract fun browseHistoryDao(): BrowseHistoryDao
    abstract fun collectionsDao(): CollectionsDao
    abstract fun countryNotesDao(): CountryNotesDao
}

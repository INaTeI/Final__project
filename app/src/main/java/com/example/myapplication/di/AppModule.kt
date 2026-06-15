package com.example.myapplication.di

import android.content.Context
import androidx.room.Room
import com.example.myapplication.data.local.AppDatabase
import com.example.myapplication.data.local.BrowseHistoryDao
import com.example.myapplication.data.local.CollectionsDao
import com.example.myapplication.data.local.CountriesDao
import com.example.myapplication.data.local.CountryNotesDao
import com.example.myapplication.data.local.FavouritesDao
import com.example.myapplication.data.local.MIGRATION_1_2
import com.example.myapplication.data.local.MIGRATION_2_3
import com.example.myapplication.data.local.UserProfilesDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase {
        return Room.databaseBuilder(
            context,
            AppDatabase::class.java,
            "countries_db"
        )
            .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
            .build()
    }

    @Provides
    fun provideCountriesDao(db: AppDatabase): CountriesDao = db.countriesDao()

    @Provides
    fun provideFavouritesDao(db: AppDatabase): FavouritesDao = db.favouritesDao()

    @Provides
    fun provideUserProfilesDao(db: AppDatabase): UserProfilesDao = db.userProfilesDao()

    @Provides
    fun provideBrowseHistoryDao(db: AppDatabase): BrowseHistoryDao = db.browseHistoryDao()

    @Provides
    fun provideCollectionsDao(db: AppDatabase): CollectionsDao = db.collectionsDao()

    @Provides
    fun provideCountryNotesDao(db: AppDatabase): CountryNotesDao = db.countryNotesDao()
}

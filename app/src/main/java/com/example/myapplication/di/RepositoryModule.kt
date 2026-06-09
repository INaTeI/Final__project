package com.example.myapplication.di

import com.example.myapplication.data.preferences.UserPreferencesRepository
import com.example.myapplication.data.repository.CountryRepositoryImpl
import com.example.myapplication.domain.preferences.AppPreferences
import com.example.myapplication.domain.preferences.RegionFilterPreferences
import com.example.myapplication.domain.repository.CountryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    abstract fun bindCountryRepository(impl: CountryRepositoryImpl): CountryRepository

    @Binds
    abstract fun bindRegionFilterPreferences(impl: UserPreferencesRepository): RegionFilterPreferences

    @Binds
    abstract fun bindAppPreferences(impl: UserPreferencesRepository): AppPreferences
}

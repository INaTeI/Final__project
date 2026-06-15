package com.example.myapplication.fake

import com.example.myapplication.data.repository.FavouritesRepository
import com.example.myapplication.data.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.every
import kotlinx.coroutines.flow.flowOf

fun createFakeFavouritesRepository(profileId: Long = 1L): Pair<FavouritesRepository, FakeFavouritesDao> {
    val dao = FakeFavouritesDao()
    val profileRepository = io.mockk.mockk<ProfileRepository>()
    every { profileRepository.observeActiveProfileId() } returns flowOf(profileId)
    coEvery { profileRepository.ensureDefaultProfile() } returns profileId
    return FavouritesRepository(dao, profileRepository) to dao
}

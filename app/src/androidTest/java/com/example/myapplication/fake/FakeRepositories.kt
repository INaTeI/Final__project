package com.example.myapplication.fake

import com.example.myapplication.data.repository.CollectionsRepository
import com.example.myapplication.data.repository.FavouritesRepository
import com.example.myapplication.data.repository.HistoryRepository
import com.example.myapplication.data.repository.NotesRepository
import com.example.myapplication.data.repository.ProfileRepository
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf

fun createTestFavouritesRepository(profileId: Long = 1L): FavouritesRepository {
    val profileRepository = mockk<ProfileRepository>()
    every { profileRepository.observeActiveProfileId() } returns flowOf(profileId)
    coEvery { profileRepository.ensureDefaultProfile() } returns profileId
    return FavouritesRepository(FakeFavouritesDao(), profileRepository)
}

fun createTestCountryDetailDependencies(
    countryRepository: com.example.myapplication.domain.repository.CountryRepository
): CountryDetailDependencies {
    val profileRepository = mockk<ProfileRepository>()
    every { profileRepository.observeActiveProfileId() } returns flowOf(1L)
    coEvery { profileRepository.ensureDefaultProfile() } returns 1L

    val historyRepository = mockk<HistoryRepository>(relaxed = true)
    val notesRepository = mockk<NotesRepository>(relaxed = true)
    every { notesRepository.observeNote(any()) } returns flowOf(null)

    val favouritesRepository = FavouritesRepository(FakeFavouritesDao(), profileRepository)
    val collectionsRepository = mockk<CollectionsRepository>(relaxed = true)
    every { collectionsRepository.observeCollections() } returns flowOf(emptyList())

    return CountryDetailDependencies(
        countryRepository = countryRepository,
        historyRepository = historyRepository,
        notesRepository = notesRepository,
        favouritesRepository = favouritesRepository,
        collectionsRepository = collectionsRepository
    )
}

data class CountryDetailDependencies(
    val countryRepository: com.example.myapplication.domain.repository.CountryRepository,
    val historyRepository: HistoryRepository,
    val notesRepository: NotesRepository,
    val favouritesRepository: FavouritesRepository,
    val collectionsRepository: CollectionsRepository
)

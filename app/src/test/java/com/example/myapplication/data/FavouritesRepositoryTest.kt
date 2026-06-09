package com.example.myapplication.data

import com.example.myapplication.fake.TestCountries
import com.example.myapplication.fake.createFakeFavouritesRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class FavouritesRepositoryTest {

    @Test
    fun duplicateAdd_doesNotCreateDuplicateEntries() = runTest {
        val (repository, dao) = createFakeFavouritesRepository()

        repository.add(TestCountries.ukraine)
        repository.add(TestCountries.ukraine)

        assertEquals(1, dao.getAllForProfile(1L).size)
    }

    @Test
    fun removeAll_clearsFavourites() = runTest {
        val (repository, dao) = createFakeFavouritesRepository()

        repository.add(TestCountries.ukraine)
        repository.add(TestCountries.poland)
        repository.removeAll()

        assertEquals(0, dao.getAllForProfile(1L).size)
    }

    @Test
    fun togglePin_updatesPinnedState() = runTest {
        val (repository, dao) = createFakeFavouritesRepository()

        repository.add(TestCountries.ukraine)
        repository.togglePin(TestCountries.ukraine)

        assertTrue(dao.getByCode(1L, TestCountries.ukraine.code)?.isPinned == true)
    }
}

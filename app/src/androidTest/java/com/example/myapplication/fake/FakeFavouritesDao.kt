package com.example.myapplication.fake

import com.example.myapplication.data.local.FavouriteCountryEntity
import com.example.myapplication.data.local.FavouritesDao
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

class FakeFavouritesDao : FavouritesDao {

    private val items = linkedMapOf<String, FavouriteCountryEntity>()
    private val flow = MutableStateFlow<List<FavouriteCountryEntity>>(emptyList())

    private fun key(profileId: Long, code: String) = "$profileId:$code"

    private fun publish() {
        flow.value = items.values.toList()
    }

    override fun observeForProfile(profileId: Long): Flow<List<FavouriteCountryEntity>> =
        flow.asStateFlow().map { list -> list.filter { it.profileId == profileId } }

    override suspend fun getAllForProfile(profileId: Long): List<FavouriteCountryEntity> =
        items.values.filter { it.profileId == profileId }

    override suspend fun getByCode(profileId: Long, code: String): FavouriteCountryEntity? =
        items[key(profileId, code)]

    override suspend fun insert(country: FavouriteCountryEntity) {
        items[key(country.profileId, country.code)] = country
        publish()
    }

    override suspend fun delete(country: FavouriteCountryEntity) {
        items.remove(key(country.profileId, country.code))
        publish()
    }

    override suspend fun deleteAllForProfile(profileId: Long) {
        items.entries.removeIf { it.value.profileId == profileId }
        publish()
    }

    override suspend fun updatePinned(profileId: Long, code: String, isPinned: Boolean) {
        val existing = items[key(profileId, code)] ?: return
        items[key(profileId, code)] = existing.copy(isPinned = isPinned)
        publish()
    }

    override suspend fun updateTag(profileId: Long, code: String, userTag: String?) {
        val existing = items[key(profileId, code)] ?: return
        items[key(profileId, code)] = existing.copy(userTag = userTag)
        publish()
    }
}

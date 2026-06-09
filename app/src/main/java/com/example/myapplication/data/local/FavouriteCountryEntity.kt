package com.example.myapplication.data.local

import androidx.room.Entity
import com.example.myapplication.domain.model.Country
import com.example.myapplication.domain.model.FavouriteCountry

@Entity(
    tableName = "favourites",
    primaryKeys = ["profileId", "code"]
)
data class FavouriteCountryEntity(
    val profileId: Long,
    val code: String,
    val name: String,
    val capital: String,
    val region: String,
    val population: Long,
    val flag: String,
    val isPinned: Boolean = false,
    val userTag: String? = null
) {
    fun toCountry() = Country(code, name, capital, region, population, flag)

    fun toFavouriteCountry() = FavouriteCountry(toCountry(), isPinned, userTag)

    companion object {
        fun fromCountry(profileId: Long, country: Country, isPinned: Boolean = false, userTag: String? = null) =
            FavouriteCountryEntity(
                profileId,
                country.code,
                country.name,
                country.capital,
                country.region,
                country.population,
                country.flag,
                isPinned,
                userTag
            )
    }
}

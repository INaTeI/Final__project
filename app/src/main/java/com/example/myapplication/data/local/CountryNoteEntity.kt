package com.example.myapplication.data.local

import androidx.room.Entity
import com.example.myapplication.domain.model.CountryNote

@Entity(
    tableName = "country_notes",
    primaryKeys = ["profileId", "countryCode"]
)
data class CountryNoteEntity(
    val profileId: Long,
    val countryCode: String,
    val text: String,
    val updatedAt: Long
) {
    fun toDomain() = CountryNote(countryCode, text, updatedAt)
}

package com.example.myapplication.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.myapplication.domain.model.UserProfile

@Entity(tableName = "user_profiles")
data class UserProfileEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long
) {
    fun toDomain() = UserProfile(id, name, createdAt)

    companion object {
        fun fromDomain(profile: UserProfile) =
            UserProfileEntity(profile.id, profile.name, profile.createdAt)
    }
}

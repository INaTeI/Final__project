package com.example.myapplication.domain.model

data class BrowseHistoryEntry(
    val id: Long,
    val countryCode: String,
    val countryName: String,
    val countryFlag: String,
    val viewedAt: Long
)

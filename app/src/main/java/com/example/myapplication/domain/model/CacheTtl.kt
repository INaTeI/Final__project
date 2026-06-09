package com.example.myapplication.domain.model

enum class CacheTtl(val hours: Int, val label: String) {
    HOURS_6(6, "6 часов"),
    HOURS_24(24, "24 часа"),
    HOURS_72(72, "3 дня");

    companion object {
        fun fromHours(hours: Int): CacheTtl =
            entries.firstOrNull { it.hours == hours } ?: HOURS_24
    }
}

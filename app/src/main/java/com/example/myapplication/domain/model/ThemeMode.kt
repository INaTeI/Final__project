package com.example.myapplication.domain.model

enum class ThemeMode(val label: String) {
    SYSTEM("Системная"),
    LIGHT("Светлая"),
    DARK("Тёмная");

    val isDark: Boolean?
        get() = when (this) {
            SYSTEM -> null
            LIGHT -> false
            DARK -> true
        }

    companion object {
        fun fromKey(key: String): ThemeMode =
            entries.firstOrNull { it.name == key } ?: SYSTEM
    }
}

package com.example.myapplication.domain.model

enum class RegionFilter(val label: String, private val apiRegion: String?) {
    ALL("Все", null),
    EUROPE("Европа", "Europe"),
    ASIA("Азия", "Asia"),
    AFRICA("Африка", "Africa"),
    AMERICAS("Америка", "Americas"),
    OCEANIA("Океания", "Oceania");

    fun matches(country: Country): Boolean =
        apiRegion == null || country.region.equals(apiRegion, ignoreCase = true)

    companion object {
        fun fromKey(key: String): RegionFilter =
            entries.firstOrNull { it.name == key } ?: ALL
    }
}

package com.example.myapplication.ui

import com.example.myapplication.domain.model.RegionFilter

object TestTags {
    const val RETRY_BUTTON = "retry_button"
    const val EMPTY_MESSAGE = "empty_message"
    const val SEARCH_FIELD = "search_field"
    const val DETAIL_TITLE = "detail_title"

    fun countryCard(code: String) = "country_card_$code"
    fun regionFilterChip(filter: RegionFilter) = "region_filter_${filter.name.lowercase()}"
}

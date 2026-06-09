package com.example.myapplication.fake

import com.example.myapplication.domain.model.Country

object TestCountries {
    val ukraine = Country(
        code = "UA",
        name = "Ukraine",
        capital = "Kyiv",
        region = "Europe",
        population = 40_000_000L,
        flag = "https://example.com/ua.png"
    )

    val poland = Country(
        code = "PL",
        name = "Poland",
        capital = "Warsaw",
        region = "Europe",
        population = 38_000_000L,
        flag = "https://example.com/pl.png"
    )
}

package com.example.eliteweatherapp

data class HourlyForecastItem(
    val time: String,
    val temp: Int,
    val iconUrl: String,
    val conditionText: String
)

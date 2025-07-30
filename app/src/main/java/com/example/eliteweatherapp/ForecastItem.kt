package com.example.eliteweatherapp

data class ForecastItem(
    val date: String,
    val avgTemp: Int,
    val iconUrl: String,
    val conditionText: String
)

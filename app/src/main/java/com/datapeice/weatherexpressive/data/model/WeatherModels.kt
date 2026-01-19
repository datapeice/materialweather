package com.datapeice.weatherexpressive.data.model

import com.google.gson.annotations.SerializedName

data class WeatherResponse(
    val current: CurrentWeather,
    val hourly: HourlyWeather,
    val daily: DailyWeather? = null // <--- ИСПРАВЛЕНО: Добавлен ? и = null
)

data class CurrentWeather(
    @SerializedName("temperature_2m") val temperature: Double,
    @SerializedName("relative_humidity_2m") val humidity: Int,
    @SerializedName("weather_code") val weatherCode: Int,
    @SerializedName("wind_speed_10m") val windSpeed: Double,
    @SerializedName("surface_pressure") val pressure: Double,
    @SerializedName("apparent_temperature") val feelsLike: Double
)

data class HourlyWeather(
    val time: List<String>,
    @SerializedName("temperature_2m") val temperatures: List<Double>
)

data class DailyWeather(
    val time: List<String>,
    @SerializedName("temperature_2m_max") val maxTemps: List<Double>,
    @SerializedName("temperature_2m_min") val minTemps: List<Double>
)

// Geocoding (оставляем как есть)
data class GeocodingResponse(val results: List<SearchResult>?)
data class SearchResult(val name: String, val latitude: Double, val longitude: Double, val country: String, val admin1: String? = null)
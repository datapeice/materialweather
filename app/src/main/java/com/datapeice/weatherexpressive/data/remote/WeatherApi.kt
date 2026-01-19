package com.datapeice.weatherexpressive.data.remote

import com.datapeice.weatherexpressive.data.model.GeocodingResponse
import com.datapeice.weatherexpressive.data.model.WeatherResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherApi {
    // ВНИМАНИЕ НА СТРОКУ НИЖЕ: Добавлен блок &daily=...
    @GET("v1/forecast?current=temperature_2m,relative_humidity_2m,weather_code,wind_speed_10m,surface_pressure,apparent_temperature&hourly=temperature_2m&daily=temperature_2m_max,temperature_2m_min&timezone=auto")
    suspend fun getForecast(
        @Query("latitude") lat: Double,
        @Query("longitude") lon: Double
    ): WeatherResponse

    @GET("https://geocoding-api.open-meteo.com/v1/search?count=10&language=en&format=json")
    suspend fun searchCity(
        @Query("name") name: String
    ): GeocodingResponse
}
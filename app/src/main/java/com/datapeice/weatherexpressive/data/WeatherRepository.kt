package com.datapeice.weatherexpressive.data.repository

import android.content.Context
import com.datapeice.weatherexpressive.data.model.SearchResult
import com.datapeice.weatherexpressive.data.model.WeatherResponse
import com.datapeice.weatherexpressive.data.remote.WeatherApi
import com.datapeice.weatherexpressive.utils.Transliterator
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherRepository(private val context: Context, private val api: WeatherApi) {
    private val gson = Gson()
    private val prefs = context.getSharedPreferences("weather_cache", Context.MODE_PRIVATE)

    // Получить погоду (Сеть -> Кэш -> Ошибка)
    suspend fun getWeather(lat: Double, lon: Double): WeatherResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response = api.getForecast(lat, lon)
                // Сохраняем в кэш
                prefs.edit().putString("last_weather", gson.toJson(response)).apply()
                response
            } catch (e: Exception) {
                // Ошибка сети? Пробуем достать кэш
                val cachedJson = prefs.getString("last_weather", null)
                if (cachedJson != null) {
                    gson.fromJson(cachedJson, WeatherResponse::class.java)
                } else {
                    throw e // Кэша нет, кидаем ошибку дальше
                }
            }
        }
    }

    // Поиск города с авто-транслитерацией
    suspend fun searchCity(query: String): List<SearchResult> {
        if (query.length < 2) return emptyList()
        // Переводим "Киев" -> "Kiev"
        val latinQuery = Transliterator.cyrillicToLatin(query)
        return api.searchCity(latinQuery).results ?: emptyList()
    }

    // Сохранение выбранного города
    fun saveSelectedCity(city: SearchResult) {
        prefs.edit()
            .putString("city_name", city.name)
            .putFloat("lat", city.latitude.toFloat())
            .putFloat("lon", city.longitude.toFloat())
            .apply()
    }
    fun saveUnit(isCelsius: Boolean) {
        prefs.edit().putBoolean("is_celsius", isCelsius).apply()
    }

    fun isCelsius(): Boolean {
        // По умолчанию true (Цельсий)
        return prefs.getBoolean("is_celsius", true)
    }
    fun getSelectedCityName(): String = prefs.getString("city_name", "Warsaw") ?: "Warsaw"
    fun getSelectedLat(): Double = prefs.getFloat("lat", 52.23f).toDouble()
    fun getSelectedLon(): Double = prefs.getFloat("lon", 21.01f).toDouble()
}
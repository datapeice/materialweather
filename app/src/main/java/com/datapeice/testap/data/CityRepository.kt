package com.datapeice.testap.data

import com.datapeice.testap.data.model.SearchResult
import com.datapeice.testap.data.remote.WeatherApi
import com.datapeice.testap.utils.Transliterator // Убедись, что создал Transliterator (см. ниже)
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class CityRepository(private val api: WeatherApi) {

    suspend fun searchCities(query: String): List<SearchResult> {
        return withContext(Dispatchers.IO) {
            try {
                // Транслитерируем запрос (Иваничи -> Ivanichi)
                val latinQuery = Transliterator.cyrillicToLatin(query)

                // Делаем запрос к API
                val response = api.searchCity(latinQuery)

                // Возвращаем список или пустой список, если ничего нет
                response.results ?: emptyList()
            } catch (e: Exception) {
                emptyList()
            }
        }
    }
}
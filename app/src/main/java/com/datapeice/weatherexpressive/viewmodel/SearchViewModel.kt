package com.datapeice.weatherexpressive.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.datapeice.weatherexpressive.data.CityRepository
import com.datapeice.weatherexpressive.data.model.SearchResult
import com.datapeice.weatherexpressive.data.remote.RetrofitClient
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class SearchViewModel(application: Application) : AndroidViewModel(application) {
    // Репозиторий создаем здесь
    private val repository = CityRepository(RetrofitClient.api)

    // Состояние списка городов
    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    private var searchJob: Job? = null

    fun onSearchQueryChanged(query: String) {
        // Отменяем предыдущий запрос, если пользователь продолжает печатать
        searchJob?.cancel()

        if (query.length < 2) {
            _searchResults.value = emptyList()
            return
        }

        searchJob = viewModelScope.launch {
            delay(500) // Задержка 0.5 сек (Debounce), чтобы не спамить API
            _isLoading.value = true
            try {
                // Repository сам сделает транслитерацию
                val results = repository.searchCities(query)
                _searchResults.value = results
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
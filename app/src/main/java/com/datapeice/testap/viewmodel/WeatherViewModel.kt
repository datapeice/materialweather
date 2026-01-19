package com.datapeice.testap.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.datapeice.testap.data.model.SearchResult
import com.datapeice.testap.data.model.WeatherResponse
import com.datapeice.testap.data.remote.RetrofitClient
import com.datapeice.testap.data.repository.WeatherRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.net.UnknownHostException

sealed class WeatherUiState {
    object Loading : WeatherUiState()
    data class Success(val data: WeatherResponse, val cityName: String) : WeatherUiState()
    data class Error(val message: String) : WeatherUiState()
}

class WeatherViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WeatherRepository(application, RetrofitClient.api)
    private val _isCelsius = MutableStateFlow(repository.isCelsius())
    val isCelsius = _isCelsius.asStateFlow()
    private val _uiState = MutableStateFlow<WeatherUiState>(WeatherUiState.Loading)
    val uiState = _uiState.asStateFlow()
    private val _currentCityName = MutableStateFlow(repository.getSelectedCityName())
    val currentCityName = _currentCityName.asStateFlow()

    // Состояние для Swipe Refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing = _isRefreshing.asStateFlow()

    private val _searchResults = MutableStateFlow<List<SearchResult>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    fun fetchWeather(isSwipeRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isSwipeRefresh) _isRefreshing.value = true else _uiState.value = WeatherUiState.Loading

            try {
                val lat = repository.getSelectedLat()
                val lon = repository.getSelectedLon()
                val name = repository.getSelectedCityName()
                _currentCityName.value = name
                // Добавляем задержку для красоты, если API отвечает мгновенно (опционально)
                // delay(500)

                val data = repository.getWeather(lat, lon)
                _uiState.value = WeatherUiState.Success(data, name)
            } catch (e: Exception) {
                val msg = when(e) {
                    is UnknownHostException -> "Нет интернета"
                    else -> "Ошибка: ${e.localizedMessage}"
                }
                _uiState.value = WeatherUiState.Error(msg)
            } finally {
                _isRefreshing.value = false
            }
        }
    }
    fun toggleUnit() {
        val newState = !_isCelsius.value
        repository.saveUnit(newState)
        _isCelsius.value = newState
        // Здесь можно перезагрузить погоду, если API требует параметр unit,
        // но пока мы просто меняем отображение (конвертацию сделаем в UI)
    }
    fun searchCity(query: String) {
        viewModelScope.launch {
            try {
                _searchResults.value = repository.searchCity(query)
            } catch (e: Exception) {
                _searchResults.value = emptyList()
            }
        }
    }

    fun selectCity(city: SearchResult) {
        repository.saveSelectedCity(city)
        fetchWeather()
    }
}
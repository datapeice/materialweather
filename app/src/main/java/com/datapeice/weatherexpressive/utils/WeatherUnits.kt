package com.datapeice.weatherexpressive.utils

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector
import com.datapeice.weatherexpressive.R
import kotlin.math.roundToInt

object WeatherUtils {

    fun formatTemp(temp: Double, isCelsius: Boolean): String {
        val value = if (isCelsius) {
            temp
        } else {
            (temp * 9 / 5) + 32
        }
        return "${value.roundToInt()}°"
    }

    fun getWeatherDescriptionResId(code: Int): Int {
        return when (code) {
            0 -> R.string.weather_clear
            1, 2, 3 -> R.string.weather_partly_cloudy
            45, 48 -> R.string.weather_fog
            51, 53, 55, 56, 57 -> R.string.weather_drizzle
            61, 63, 65, 66, 67, 80, 81, 82 -> R.string.weather_rain
            71, 73, 75, 77, 85, 86 -> R.string.weather_snow
            95, 96, 99 -> R.string.weather_thunderstorm
            else -> R.string.weather_cloudy
        }
    }

    // НОВОЕ: Подбор иконки
    fun getWeatherIcon(code: Int): ImageVector {
        return when (code) {
            0 -> Icons.Rounded.WbSunny            // Ясно
            1, 2, 3 -> Icons.Rounded.Cloud        // Облачно
            45, 48 -> Icons.Rounded.Dehaze        // Туман
            51, 53, 55, 61, 63 -> Icons.Rounded.WaterDrop // Дождь
            71, 73, 75, 77 -> Icons.Rounded.AcUnit // Снег
            95, 96, 99 -> Icons.Rounded.Thunderstorm // Гроза
            else -> Icons.Rounded.WbCloudy        // Дефолт
        }
    }
}
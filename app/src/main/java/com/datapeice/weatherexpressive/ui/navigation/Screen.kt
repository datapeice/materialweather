package com.datapeice.weatherexpressive.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(val route: String, val title: String, val icon: ImageVector) {
    object Weather : Screen("weather", "Погода", Icons.Rounded.WbSunny)
    object Settings : Screen("settings", "Настройки", Icons.Rounded.Settings)
}
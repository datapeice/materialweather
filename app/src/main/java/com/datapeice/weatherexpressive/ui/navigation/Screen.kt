package com.datapeice.weatherexpressive.ui.navigation

import androidx.annotation.StringRes
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.ui.graphics.vector.ImageVector
import com.datapeice.weatherexpressive.R

sealed class Screen(val route: String, @StringRes val title: Int, val icon: ImageVector) {
    object Weather : Screen("weather", R.string.weather, Icons.Rounded.WbSunny)
    object Settings : Screen("settings", R.string.settings_title, Icons.Rounded.Settings)
}
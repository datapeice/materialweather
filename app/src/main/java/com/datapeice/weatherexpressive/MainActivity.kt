package com.datapeice.weatherexpressive

import android.graphics.Color as AndroidColor
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.datapeice.weatherexpressive.data.ThemeSettings
import com.datapeice.weatherexpressive.ui.navigation.Screen
import com.datapeice.weatherexpressive.ui.screens.SettingsScreen
import com.datapeice.weatherexpressive.ui.screens.WeatherScreen
import com.datapeice.weatherexpressive.ui.theme.WeatherExpressiveTheme
import com.datapeice.weatherexpressive.viewmodel.WeatherViewModel
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.auto(AndroidColor.TRANSPARENT, AndroidColor.TRANSPARENT)
        )
        val themeSettings = ThemeSettings(this)

        setContent {
            val themeOption by themeSettings.themeFlow.collectAsState(initial = 0)
            val isDark = when (themeOption) {
                1 -> false
                2 -> true
                else -> isSystemInDarkTheme()
            }

            val navController = rememberNavController()

            Crossfade(
                targetState = isDark,
                animationSpec = tween(durationMillis = 500),
                label = "ThemeTransition"
            ) { dark ->
                WeatherExpressiveTheme(darkTheme = dark) {
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = MaterialTheme.colorScheme.background
                    ) {
                        MainApp(
                            navController = navController,
                            currentTheme = themeOption,
                            onThemeChange = { option ->
                                lifecycleScope.launch { themeSettings.saveTheme(option) }
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainApp(
    navController: NavHostController,
    currentTheme: Int,
    onThemeChange: (Int) -> Unit
) {
    val items = listOf(Screen.Weather, Screen.Settings)

    Scaffold(
        bottomBar = {
            // Контейнер позиционирования
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Растягиваем контейнер, чтобы выровнять контент по центру
                    .padding(bottom = 30.dp), // Отступ только снизу
                contentAlignment = Alignment.Center // Центрируем сам бар
            ) {
                SlidingNavigationBar(
                    items = items,
                    navController = navController,
                    // ИСПРАВЛЕНИЕ: Задаем фиксированную ширину для компактности
                    modifier = Modifier.width(260.dp)
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Weather.route,
            modifier = Modifier.fillMaxSize()
        ) {
            composable(Screen.Weather.route) {
                val viewModel: WeatherViewModel = viewModel()
                LaunchedEffect(Unit) {
                    if (viewModel.uiState.value is com.datapeice.weatherexpressive.viewmodel.WeatherUiState.Loading) {
                        viewModel.fetchWeather()
                    }
                }
                WeatherScreen(
                    viewModel = viewModel,
                    contentPadding = innerPadding
                )
            }
            composable(Screen.Settings.route) {
                SettingsScreen(
                    currentTheme = currentTheme,
                    onThemeChange = onThemeChange
                )
            }
        }
    }
}

@Composable
fun SlidingNavigationBar(
    items: List<Screen>,
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route

    val selectedIndex = items.indexOfFirst { it.route == currentRoute }.takeIf { it >= 0 } ?: 0

    Surface(
        shape = RoundedCornerShape(50),
        // ИСПРАВЛЕНИЕ: Используем surfaceContainerHigh для непрозрачности и контраста
        color = MaterialTheme.colorScheme.surfaceContainerHigh,
        tonalElevation = 3.dp,
        shadowElevation = 8.dp,
        // ИСПРАВЛЕНИЕ: Уменьшили высоту до 56dp для компактности
        modifier = modifier.height(56.dp)
    ) {
        BoxWithConstraints(
            modifier = Modifier.padding(5.dp) // Чуть меньше отступ внутри
        ) {
            val maxWidth = maxWidth
            val itemWidth = maxWidth / items.size

            // Курсор (Фон)
            val indicatorOffset by animateDpAsState(
                targetValue = itemWidth * selectedIndex,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioLowBouncy,
                    stiffness = Spring.StiffnessLow
                ),
                label = "indicatorOffset"
            )

            Box(
                modifier = Modifier
                    .offset(x = indicatorOffset)
                    .width(itemWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(50))
                    .background(MaterialTheme.colorScheme.secondaryContainer)
            )

            // Контент (Иконки и текст)
            Row(modifier = Modifier.fillMaxSize()) {
                items.forEachIndexed { index, screen ->
                    val isSelected = index == selectedIndex

                    val contentColor by animateColorAsState(
                        targetValue = if (isSelected) MaterialTheme.colorScheme.onSecondaryContainer
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                        animationSpec = tween(durationMillis = 300),
                        label = "textColor"
                    )

                    Box(
                        modifier = Modifier
                            .width(itemWidth)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(50))
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null
                            ) {
                                navController.navigate(screen.route) {
                                    popUpTo(navController.graph.startDestinationId) { saveState = true }
                                    launchSingleTop = true
                                    restoreState = true
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = screen.icon,
                                contentDescription = null,
                                tint = contentColor,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = screen.title,
                                style = MaterialTheme.typography.labelMedium.copy( // Уменьшили шрифт для компактности
                                    fontWeight = FontWeight.Bold
                                ),
                                color = contentColor,
                                maxLines = 1
                            )
                        }
                    }
                }
            }
        }
    }
}
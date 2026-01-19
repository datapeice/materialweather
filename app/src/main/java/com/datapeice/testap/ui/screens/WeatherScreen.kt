package com.datapeice.testap.ui.screens

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.datapeice.testap.R
import com.datapeice.testap.utils.WeatherUtils
import com.datapeice.testap.viewmodel.WeatherUiState
import com.datapeice.testap.viewmodel.WeatherViewModel
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WeatherScreen(
    viewModel: WeatherViewModel,
    contentPadding: PaddingValues = PaddingValues(0.dp)
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isCelsius by viewModel.isCelsius.collectAsStateWithLifecycle()
    val isRefreshing by viewModel.isRefreshing.collectAsStateWithLifecycle()

    val haptics = LocalHapticFeedback.current

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollState = rememberScrollState()

    // Порог срабатывания анимации
    val showMiniHeader by remember {
        derivedStateOf { scrollState.value > 600 }
    }

    // --- АНИМАЦИИ ДЛЯ ХЕДЕРА ---

    // 1. Прозрачность (Alpha): 0 -> 1
    val headerAlpha by animateFloatAsState(
        targetValue = if (showMiniHeader) 1f else 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow), // Плавная пружина
        label = "headerAlpha"
    )

    // 2. Сдвиг сверху (Slide Y): -20dp -> 0dp
    val headerOffset by animateDpAsState(
        targetValue = if (showMiniHeader) 0.dp else (-20).dp,
        animationSpec = spring(stiffness = Spring.StiffnessLow),
        label = "headerSlide"
    )

    LaunchedEffect(state) {
        if (state is WeatherUiState.Success) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = MaterialTheme.colorScheme.background,
        // Фон на весь экран (под системные бары)
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            TopAppBar(
                title = {
                    // Мы не используем if/else для скрытия, чтобы не было "черного скачка".
                    // Элемент есть всегда, но он прозрачный и сдвинут вверх.
                    if (state is WeatherUiState.Success) {
                        val data = (state as WeatherUiState.Success).data
                        val city = (state as WeatherUiState.Success).cityName
                        val tempStr = WeatherUtils.formatTemp(data.current.temperature, isCelsius)

                        // Контейнер с анимацией
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .offset(y = headerOffset) // Применяем сдвиг
                                .alpha(headerAlpha),      // Применяем прозрачность
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = city,
                                style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                color = MaterialTheme.colorScheme.onBackground
                            )
                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = tempStr,
                                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
                                    color = MaterialTheme.colorScheme.onBackground
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = WeatherUtils.getWeatherIcon(data.current.weatherCode),
                                    contentDescription = null,
                                    modifier = Modifier.size(30.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent,
                    // Фон при скролле (можно сделать полупрозрачным, если нужно)
                    scrolledContainerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.95f),
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.fetchWeather(isSwipeRefresh = true) },
                modifier = Modifier.fillMaxSize()
            ) {
                Box(modifier = Modifier.fillMaxSize()) {
                    when (val s = state) {
                        is WeatherUiState.Loading -> {
                            if (!isRefreshing) {
                                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        is WeatherUiState.Error -> {
                            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                                ErrorState(s.message) { viewModel.fetchWeather() }
                            }
                        }
                        is WeatherUiState.Success -> {
                            WeatherExpressiveContent(
                                data = s.data,
                                cityName = s.cityName,
                                isCelsius = isCelsius,
                                scrollState = scrollState,
                                bottomPadding = contentPadding.calculateBottomPadding()
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherExpressiveContent(
    data: com.datapeice.testap.data.model.WeatherResponse,
    cityName: String,
    isCelsius: Boolean,
    scrollState: androidx.compose.foundation.ScrollState,
    bottomPadding: androidx.compose.ui.unit.Dp
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.windowInsetsTopHeight(WindowInsets.statusBars))

        Box(
            modifier = Modifier.padding(top = 20.dp, bottom = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = stringResource(R.string.app_name),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )
        }

        Spacer(modifier = Modifier.height(30.dp))

        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.surfaceContainerHigh,
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(Icons.Rounded.LocationOn, null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text(cityName, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = WeatherUtils.formatTemp(data.current.temperature, isCelsius),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 110.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = (-4).sp
                ),
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.width(16.dp))

            Icon(
                imageVector = WeatherUtils.getWeatherIcon(data.current.weatherCode),
                contentDescription = null,
                modifier = Modifier.size(80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }

        Text(
            text = stringResource(id = WeatherUtils.getWeatherDescriptionResId(data.current.weatherCode)),
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.secondary
        )

        Spacer(modifier = Modifier.height(40.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ExpressiveDetailCard(
                icon = Icons.Rounded.Air,
                value = "${data.current.windSpeed}",
                unit = stringResource(R.string.wind),
                label = stringResource(R.string.wind),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier.weight(1f)
            )
            ExpressiveDetailCard(
                icon = Icons.Rounded.WaterDrop,
                value = "${data.current.humidity}",
                unit = "%",
                label = stringResource(R.string.humidity),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.secondary,
                modifier = Modifier.weight(1f)
            )
            ExpressiveDetailCard(
                icon = Icons.Rounded.Thermostat,
                value = WeatherUtils.formatTemp(data.current.feelsLike, isCelsius),
                unit = "",
                label = stringResource(R.string.feels_like),
                containerColor = MaterialTheme.colorScheme.surfaceContainer,
                contentColor = MaterialTheme.colorScheme.tertiary,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        SectionHeader(stringResource(R.string.today))

        val currentHour = LocalTime.now().hour
        val futureTemps = data.hourly.temperatures.drop(currentHour).take(24)

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(futureTemps.size) { index ->
                val displayHour = (currentHour + index) % 24
                HourlyPill(
                    time = String.format("%02d:00", displayHour),
                    temp = WeatherUtils.formatTemp(futureTemps[index], isCelsius),
                    isCurrent = index == 0
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        SectionHeader(stringResource(R.string.weekly))

        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            val today = LocalDate.now()
            (0..6).forEach { i ->
                val date = today.plusDays(i.toLong())
                val dayName = date.format(DateTimeFormatter.ofPattern("EEEE", Locale.getDefault()))
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                val dummyMin = -2.0 + i
                val dummyMax = 4.0 + i

                DailyForecastRow(
                    day = dayName,
                    min = WeatherUtils.formatTemp(dummyMin, isCelsius),
                    max = WeatherUtils.formatTemp(dummyMax, isCelsius)
                )
            }
        }

        Spacer(modifier = Modifier.height(bottomPadding + 16.dp))
        Spacer(modifier = Modifier.windowInsetsBottomHeight(WindowInsets.navigationBars))
    }
}

// ... Компоненты (Cards, Pills...) без изменений ...

@Composable
fun ExpressiveDetailCard(
    icon: ImageVector,
    value: String,
    unit: String,
    label: String,
    containerColor: Color,
    contentColor: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.height(140.dp),
        shape = RoundedCornerShape(32.dp),
        color = containerColor
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = contentColor
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (unit.isNotEmpty()) {
                    Text(
                        text = unit,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
fun HourlyPill(time: String, temp: String, isCurrent: Boolean) {
    Surface(
        modifier = Modifier.size(65.dp, 110.dp),
        shape = RoundedCornerShape(100.dp),
        color = if (isCurrent) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceContainerHigh
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.padding(vertical = 12.dp)
        ) {
            Text(
                time,
                style = MaterialTheme.typography.labelSmall,
                color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
            Text(
                temp,
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = if (isCurrent) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
fun DailyForecastRow(day: String, min: String, max: String) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceContainer
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(day, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(min, color = MaterialTheme.colorScheme.secondary)
                Box(
                    modifier = Modifier
                        .padding(horizontal = 12.dp)
                        .width(80.dp)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .fillMaxHeight()
                            .align(Alignment.Center)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
                Text(max, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Bold),
        modifier = Modifier.padding(bottom = 16.dp, start = 4.dp)
    )
}

@Composable
fun ErrorState(message: String, onRetry: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(Icons.Rounded.CloudOff, null, modifier = Modifier.size(64.dp), tint = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Text(message, textAlign = TextAlign.Center, color = MaterialTheme.colorScheme.error)
        Spacer(Modifier.height(16.dp))
        Button(onClick = onRetry) { Text(stringResource(R.string.retry)) }
    }
}
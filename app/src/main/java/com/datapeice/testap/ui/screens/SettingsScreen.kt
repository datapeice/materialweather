package com.datapeice.testap.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.datapeice.testap.R
import com.datapeice.testap.viewmodel.WeatherViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    currentTheme: Int,
    onThemeChange: (Int) -> Unit
) {
    val viewModel: WeatherViewModel = viewModel()
    val currentCity by viewModel.currentCityName.collectAsStateWithLifecycle()
    val isCelsius by viewModel.isCelsius.collectAsStateWithLifecycle()

    var showSearchDialog by remember { mutableStateOf(false) }

    val haptics = LocalHapticFeedback.current
    val uriHandler = LocalUriHandler.current

    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())
    val backgroundColor = MaterialTheme.colorScheme.background
    val groupColor = MaterialTheme.colorScheme.surfaceContainer

    if (showSearchDialog) {
        SearchCityDialog(
            viewModel = viewModel,
            onDismiss = { showSearchDialog = false },
            onCitySelected = { city ->
                viewModel.selectCity(city)
                showSearchDialog = false
                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            }
        )
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .nestedScroll(scrollBehavior.nestedScrollConnection),
        containerColor = backgroundColor,
        // Чтобы фон был под системными барами (как в WeatherScreen)
        contentWindowInsets = WindowInsets(0, 0, 0, 0),
        topBar = {
            LargeTopAppBar(
                title = {
                    Text(
                        text = stringResource(R.string.settings_title),
                        fontWeight = FontWeight.Bold,
                        fontSize = MaterialTheme.typography.displaySmall.fontSize
                    )
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = Color.Transparent, // Прозрачный, чтобы видеть фон
                    scrolledContainerColor = backgroundColor,
                    titleContentColor = MaterialTheme.colorScheme.onBackground
                ),
                scrollBehavior = scrollBehavior
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(padding) // Отступ от TopBar
                .padding(horizontal = 20.dp)
            // ИСПРАВЛЕНИЕ: Убрали verticalArrangement = Arrangement.spacedBy(...),
            // чтобы управлять отступами вручную
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // ==========================
            // --- СЕКЦИЯ 1: ПОГОДА ---
            // ==========================
            SectionTitle(stringResource(R.string.section_weather))

            // Маленький отступ между заголовком и карточкой
            Spacer(modifier = Modifier.height(8.dp))

            ExpressiveSettingsGroup(groupColor) {
                ExpressiveSettingsItem(
                    icon = Icons.Rounded.LocationCity,
                    title = stringResource(R.string.pick_city),
                    value = currentCity,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        showSearchDialog = true
                    },
                    isLast = false
                )
                ExpressiveSettingsItem(
                    icon = Icons.Rounded.Thermostat,
                    title = stringResource(R.string.units),
                    value = if (isCelsius) stringResource(R.string.celsius) else stringResource(R.string.fahrenheit),
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        viewModel.toggleUnit()
                    },
                    isLast = true
                )
            }

            // БОЛЬШОЙ ОТСТУП МЕЖДУ СЕКЦИЯМИ
            Spacer(modifier = Modifier.height(32.dp))

            // ==========================
            // --- СЕКЦИЯ 2: ОФОРМЛЕНИЕ ---
            // ==========================
            SectionTitle(stringResource(R.string.section_appearance))

            Spacer(modifier = Modifier.height(8.dp))

            ExpressiveSettingsGroup(groupColor) {
                val themeStateText = when (currentTheme) {
                    1 -> stringResource(R.string.off)
                    2 -> stringResource(R.string.on)
                    else -> stringResource(R.string.system)
                }

                ExpressiveSettingsItem(
                    icon = Icons.Rounded.DarkMode,
                    title = stringResource(R.string.dark_theme),
                    value = themeStateText,
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        onThemeChange(if (currentTheme == 2) 1 else 2)
                    },
                    isLast = true
                )
            }

            // БОЛЬШОЙ ОТСТУП МЕЖДУ СЕКЦИЯМИ
            Spacer(modifier = Modifier.height(32.dp))

            // ==========================
            // --- СЕКЦИЯ 3: ИНФО ---
            // ==========================
            SectionTitle(stringResource(R.string.section_about))

            Spacer(modifier = Modifier.height(8.dp))

            ExpressiveSettingsGroup(groupColor) {
                ExpressiveSettingsItem(
                    icon = Icons.Rounded.Person,
                    title = stringResource(R.string.developer),
                    value = "@datapeice",
                    onClick = {},
                    isLast = false
                )
                ExpressiveSettingsItem(
                    icon = Icons.Rounded.Public,
                    title = stringResource(R.string.website),
                    value = "datapeice.me",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        uriHandler.openUri("https://datapeice.me")
                    },
                    isLast = false
                )
                ExpressiveSettingsItem(
                    icon = Icons.Rounded.AccountTree,
                    title = stringResource(R.string.repository),
                    value = "GitHub",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        uriHandler.openUri("https://github.com/datapeice/materialweather")
                    },
                    isLast = false
                )
                ExpressiveSettingsItem(
                    icon = Icons.Rounded.Email,
                    title = stringResource(R.string.contact),
                    value = "Mail",
                    onClick = {
                        haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                        uriHandler.openUri("mailto:elijah@datapeice.me?subject=Weather Expressive%3A%20")
                    },
                    isLast = false
                )
                ExpressiveSettingsItem(
                    icon = Icons.Rounded.Info,
                    title = stringResource(R.string.version),
                    value = "1.0.1",
                    onClick = {},
                    isLast = true
                )
            }

            // Отступ снизу для навигации
            Spacer(modifier = Modifier.height(120.dp))
        }
    }
}

// --- ДИАЛОГ ПОИСКА (Без изменений) ---
@Composable
fun SearchCityDialog(
    viewModel: WeatherViewModel,
    onDismiss: () -> Unit,
    onCitySelected: (com.datapeice.testap.data.model.SearchResult) -> Unit
) {
    var query by remember { mutableStateOf("") }
    val results by viewModel.searchResults.collectAsStateWithLifecycle()
    val haptics = LocalHapticFeedback.current

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.fillMaxWidth().height(500.dp)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = stringResource(R.string.search_city),
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )
                Spacer(Modifier.height(16.dp))

                OutlinedTextField(
                    value = query,
                    onValueChange = {
                        query = it
                        viewModel.searchCity(it)
                    },
                    label = { Text(stringResource(R.string.search_placeholder)) },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    singleLine = true
                )

                Spacer(Modifier.height(8.dp))

                LazyColumn {
                    items(results) { city ->
                        ListItem(
                            headlineContent = { Text(city.name, fontWeight = FontWeight.Bold) },
                            supportingContent = {
                                val region = city.admin1?.let { "$it, " } ?: ""
                                Text("$region${city.country}")
                            },
                            leadingContent = { Icon(Icons.Rounded.LocationOn, null) },
                            modifier = Modifier.clickable {
                                haptics.performHapticFeedback(HapticFeedbackType.LongPress)
                                onCitySelected(city)
                            }
                        )
                    }
                }
            }
        }
    }
}

// --- КОМПОНЕНТЫ ---

@Composable
fun ExpressiveSettingsGroup(
    color: Color,
    content: @Composable ColumnScope.() -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        color = color,
    ) {
        Column(modifier = Modifier.padding(vertical = 4.dp)) {
            content()
        }
    }
}

@Composable
fun SectionTitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(start = 12.dp) // Убрали bottom padding тут, так как используем Spacer
    )
}

@Composable
fun ExpressiveSettingsItem(
    icon: ImageVector,
    title: String,
    value: String?,
    onClick: () -> Unit,
    isLast: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            shape = CircleShape,
            color = MaterialTheme.colorScheme.primaryContainer,
            modifier = Modifier.size(36.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer
                )
            }
        }

        Spacer(modifier = Modifier.width(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Medium),
            modifier = Modifier.weight(1f),
            color = MaterialTheme.colorScheme.onSurface
        )

        if (value != null) {
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }

    if (!isLast) {
        HorizontalDivider(
            modifier = Modifier.padding(start = 72.dp),
            thickness = 0.5.dp,
            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    }
}
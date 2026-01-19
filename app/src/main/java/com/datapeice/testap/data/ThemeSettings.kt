package com.datapeice.testap.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Расширение для DataStore (создается один раз на уровне файла)
val Context.dataStore by preferencesDataStore(name = "settings")

class ThemeSettings(private val context: Context) {

    // Приватный ключ
    private val THEME_KEY = intPreferencesKey("theme_option")

    // --- ИСПРАВЛЕНИЕ ЗДЕСЬ ---
    // Это свойство должно быть PUBLIC (по умолчанию в Kotlin так и есть),
    // чтобы MainActivity мог его читать.
    val themeFlow: Flow<Int> = context.dataStore.data.map { preferences ->
        preferences[THEME_KEY] ?: 0 // По умолчанию 0 (Системная тема)
    }

    suspend fun saveTheme(option: Int) {
        context.dataStore.edit { preferences ->
            preferences[THEME_KEY] = option
        }
    }
}
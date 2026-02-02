package com.example.viewmodelkotlinapp.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(
    name = "settings"
)

// Uses DataStore for persistent storage
interface SettingsRepository {
    val isDarkTheme: Flow<Boolean>
    suspend fun setDarkTheme(isDark: Boolean)
}

// DataStore implementation of SettingsRepository
// Provides persistent storage for app settings
class DataStoreSettingsRepository(
    private val context: Context
) : SettingsRepository {

    private object PreferencesKeys {
        val DARK_THEME = booleanPreferencesKey("dark_theme")
    }

    // Emits current theme setting and updates automatically
    override val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .catch { exception ->
            // Handle errors reading preferences
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.DARK_THEME] ?: false
        }


    // Update dark theme preference
    // Persists to DataStore
    override suspend fun setDarkTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[PreferencesKeys.DARK_THEME] = isDark
        }
    }
}
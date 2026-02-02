package com.example.viewmodelkotlinapp.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.viewmodelkotlinapp.data.SettingsRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

// ViewModel for Settings Screen
class SettingsViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val _error = MutableStateFlow<String?>(null)


    // UI State combining settings from repository
    val uiState: StateFlow<SettingsUiState> = combine(
        settingsRepository.isDarkTheme,
        _error
    ) { isDarkTheme, error ->
        SettingsUiState(
            isDarkTheme = isDarkTheme,
            error = error
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsUiState()
    )


    // Toggle between dark and light theme
    fun onToggleTheme() {
        viewModelScope.launch {
            try {
                val currentTheme = uiState.value.isDarkTheme
                settingsRepository.setDarkTheme(!currentTheme)
            } catch (e: Exception) {
                _error.value = "Failed to update theme: ${e.message}"
            }
        }
    }


    // Set specific theme (dark or light)
    fun onSetTheme(isDark: Boolean) {
        viewModelScope.launch {
            try {
                settingsRepository.setDarkTheme(isDark)
            } catch (e: Exception) {
                _error.value = "Failed to update theme: ${e.message}"
            }
        }
    }


    // Clear error message
    fun onDismissError() {
        _error.value = null
    }
}
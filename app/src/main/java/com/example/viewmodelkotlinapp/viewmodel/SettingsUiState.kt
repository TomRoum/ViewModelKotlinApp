package com.example.viewmodelkotlinapp.viewmodel

// UI State for Settings Screen
// Contains all settings preferences and UI state
data class SettingsUiState(
    val isDarkTheme: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null
)
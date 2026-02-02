package com.example.viewmodelkotlinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.viewmodelkotlinapp.di.SettingsViewModelFactory
import com.example.viewmodelkotlinapp.navigation.TaskApp
import com.example.viewmodelkotlinapp.ui.theme.ViewModelKotlinAppTheme
import com.example.viewmodelkotlinapp.viewmodel.SettingsViewModel

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            // Get settings to apply theme
            val settingsViewModel: SettingsViewModel = viewModel(
                factory = SettingsViewModelFactory(applicationContext)
            )
            val settingsState by settingsViewModel.uiState.collectAsStateWithLifecycle()

            // Apply theme based on settings
            ViewModelKotlinAppTheme(
                darkTheme = settingsState.isDarkTheme
            ) {
                TaskApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
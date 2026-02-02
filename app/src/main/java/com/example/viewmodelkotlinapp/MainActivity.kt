package com.example.viewmodelkotlinapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.ui.Modifier
import com.example.viewmodelkotlinapp.navigation.TaskApp
import com.example.viewmodelkotlinapp.ui.theme.ViewModelKotlinAppTheme

/**
 * Main Activity - Entry point of the app
 * Sets up navigation and theming
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ViewModelKotlinAppTheme {
                TaskApp(modifier = Modifier.fillMaxSize())
            }
        }
    }
}
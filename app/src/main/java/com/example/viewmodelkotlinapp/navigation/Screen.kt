package com.example.viewmodelkotlinapp.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

sealed class Screen(
    val route: String,
    val title: String,
    val icon: ImageVector
) {
    // Home screen
    data object Home : Screen(
        route = "home",
        title = "Tasks",
        icon = Icons.Default.Home
    )

    // Calendar screen
    data object Calendar : Screen(
        route = "calendar",
        title = "Calendar",
        icon = Icons.Default.CalendarToday
    )

    // Settings screen - App preferences and theme toggle
    data object Settings : Screen(
        route = "settings",
        title = "Settings",
        icon = Icons.Default.Settings
    )

    companion object {

        val screens = listOf(Home, Calendar, Settings)
    }
}
package com.example.viewmodelkotlinapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.viewmodelkotlinapp.ui.CalendarScreen
import com.example.viewmodelkotlinapp.ui.HomeScreen
import com.example.viewmodelkotlinapp.ui.SettingsScreen

@Composable
fun TaskApp(
    modifier: Modifier = Modifier,
    navController: NavHostController = rememberNavController()
) {
    Scaffold(
        modifier = modifier,
        bottomBar = {
            BottomNavigationBar(navController = navController)
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            // Home Screen
            composable(route = Screen.Home.route) {
                HomeScreen()
            }

            // Calendar Screen
            composable(route = Screen.Calendar.route) {
                CalendarScreen()
            }

            // Settings Screen
            composable(route = Screen.Settings.route) {
                SettingsScreen()
            }
        }
    }
}
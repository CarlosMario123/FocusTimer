package com.example.focustimer

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.example.focustimer.core.navigation.NavigationConstants
import com.example.focustimer.feature_history.presentation.screen.HistoryScreen
import com.example.focustimer.feature_notifications.presentation.screen.NotificationsScreen

import com.example.focustimer.feature_onboarding.presentation.screen.OnboardingScreen
import com.example.focustimer.feature_settings.presentation.screen.SettingsScreen
import com.example.focustimer.feature_timer.presentation.screen.TimerScreen


@Composable
fun AppNavHost(
    navController: NavHostController,
    startDestination: String
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        composable(NavigationConstants.ROUTE_ONBOARDING) {
            OnboardingScreen(navController = navController)
        }

        composable(NavigationConstants.ROUTE_TIMER) {
            TimerScreen(navController = navController)
        }

        composable(NavigationConstants.ROUTE_SETTINGS) {
            SettingsScreen(navController = navController)
        }

        composable(NavigationConstants.ROUTE_NOTIFICATIONS) {
            NotificationsScreen(navController = navController)
        }

        composable(NavigationConstants.ROUTE_HISTORY) {
            HistoryScreen(navController = navController)
        }
    }
}
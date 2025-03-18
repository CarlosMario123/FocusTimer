package com.example.focustimer

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.rememberNavController
import com.example.focustimer.core.navigation.NavigationConstants
import com.example.focustimer.core.presentation.components.BottomNavBar

@Composable
fun FocusTimerApp(onboardingCompleted: Boolean) {
    val navController = rememberNavController()

    val startDestination = if (onboardingCompleted) {
        NavigationConstants.ROUTE_TIMER
    } else {
        NavigationConstants.ROUTE_ONBOARDING
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        bottomBar = {
            BottomNavBar(navController = navController)
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            AppNavHost(
                navController = navController,
                startDestination = startDestination
            )
        }
    }
}
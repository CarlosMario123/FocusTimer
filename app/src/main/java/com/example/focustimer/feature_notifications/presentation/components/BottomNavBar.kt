package com.example.focustimer.core.presentation.components

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.focustimer.core.navigation.NavigationConstants


data class BottomNavItem(
    val route: String,
    val title: String,
    val icon: ImageVector
)


@Composable
fun BottomNavBar(
    navController: NavController,
    modifier: Modifier = Modifier
) {
    // Definir los ítems de la barra de navegación
    val items = listOf(
        BottomNavItem(
            route = NavigationConstants.ROUTE_TIMER,
            title = "Timer",
            icon = Icons.Default.Timer
        ),
        BottomNavItem(
            route = NavigationConstants.ROUTE_HISTORY,
            title = "Historial",
            icon = Icons.Default.History
        ),
        BottomNavItem(
            route = NavigationConstants.ROUTE_SETTINGS,
            title = "Ajustes",
            icon = Icons.Default.Settings
        )
    )

    // Obtener la ruta actual
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination

    // Verificar si estamos en una ruta principal para mostrar o no la barra
    val shouldShowBottomBar = items.any { item ->
        currentDestination?.hierarchy?.any { it.route == item.route } == true
    }

    if (shouldShowBottomBar) {
        NavigationBar(
            modifier = modifier,
            containerColor = Color(0xFF252738),
            contentColor = Color.White
        ) {
            items.forEach { item ->
                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true

                NavigationBarItem(
                    icon = { Icon(item.icon, contentDescription = item.title) },
                    label = { Text(item.title) },
                    selected = selected,
                    onClick = {
                        navController.navigate(item.route) {
                            // Para evitar crear múltiples copias de la misma pantalla en el stack
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Restaurar el estado si ya existe
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = Color(0xFF6C63FF),
                        selectedTextColor = Color(0xFF6C63FF),
                        indicatorColor = Color(0xFF1A1B2B),
                        unselectedIconColor = Color.Gray,
                        unselectedTextColor = Color.Gray
                    )
                )
            }
        }
    }
}
package com.praisetechzw.netindicator.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.ui.graphics.vector.ImageVector

data class BottomNavItem(
    val screen: Screen,
    val label: String,
    val icon: ImageVector
)

val bottomNavItems = listOf(
    BottomNavItem(Screen.Dashboard, "Dashboard", Icons.Filled.Home),
    BottomNavItem(Screen.History, "History", Icons.Filled.History),
    BottomNavItem(Screen.Stats, "Stats", Icons.Filled.BarChart),
    BottomNavItem(Screen.Settings, "Settings", Icons.Filled.Settings)
)

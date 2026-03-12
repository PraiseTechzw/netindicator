package com.praisetechzw.netindicator.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.praisetechzw.netindicator.ui.dashboard.DashboardScreen
import com.praisetechzw.netindicator.ui.history.HistoryScreen
import com.praisetechzw.netindicator.ui.settings.SettingsScreen
import com.praisetechzw.netindicator.ui.stats.StatsScreen

@Composable
fun NetIndicatorNavGraph(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = Screen.Dashboard.route
    ) {
        composable(Screen.Dashboard.route) { DashboardScreen() }
        composable(Screen.History.route) { HistoryScreen() }
        composable(Screen.Stats.route) { StatsScreen() }
        composable(Screen.Settings.route) { SettingsScreen() }
    }
}

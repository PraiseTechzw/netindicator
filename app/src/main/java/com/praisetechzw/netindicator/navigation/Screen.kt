package com.praisetechzw.netindicator.navigation

sealed class Screen(val route: String) {
    object Dashboard : Screen("dashboard")
    object History : Screen("history")
    object Stats : Screen("stats")
    object Settings : Screen("settings")
}

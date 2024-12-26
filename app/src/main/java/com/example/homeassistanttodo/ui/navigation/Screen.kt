package com.example.homeassistanttodo.ui.navigation

sealed class Screen(val route: String) {
    object Connection : Screen("connection")
    object Settings : Screen("settings")
    object Todo : Screen("todo")
}
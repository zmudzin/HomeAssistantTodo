package com.example.homeassistanttodo.ui.navigation

sealed class Screen(val route: String) {
    object Connection : Screen("connection")
    object ServerSettings : Screen("settings")
}
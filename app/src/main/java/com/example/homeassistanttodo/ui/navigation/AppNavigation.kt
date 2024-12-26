package com.example.homeassistanttodo.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.homeassistanttodo.ui.connection.ConnectionScreen
import com.example.homeassistanttodo.ui.todo.TodoScreen

@Composable
fun AppNavigation() {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.Connection.route) {
        composable(Screen.Connection.route) {
            ConnectionScreen(
                onSettingsClick = { },
                onNavigateToTodo = { navController.navigate(Screen.Todo.route) }
            )
        }
        composable(Screen.Todo.route) {
            TodoScreen()
        }
    }
}
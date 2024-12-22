package com.example.homeassistanttodo.ui.connection

data class ConnectionUiState(
    val serverUrl: String = "",
    val token: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val connectionStatus: String = "Disconnected"
)
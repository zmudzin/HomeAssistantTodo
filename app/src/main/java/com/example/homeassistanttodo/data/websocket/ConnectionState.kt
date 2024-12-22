package com.example.homeassistanttodo.data.websocket

import com.example.homeassistanttodo.ui.connection.ConnectionUiState

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connected : ConnectionState()
    object Authenticated : ConnectionState()
    data class Error(val message: String) : ConnectionState()

    fun toUiState(serverUrl: String, token: String): ConnectionUiState {
        return when (this) {
            is Connected -> ConnectionUiState(
                serverUrl = serverUrl,
                token = token,
                connectionStatus = "Connected"
            )
            is Authenticated -> ConnectionUiState(
                serverUrl = serverUrl,
                token = token,
                connectionStatus = "Authenticated"
            )
            is Disconnected -> ConnectionUiState(
                serverUrl = serverUrl,
                token = token,
                connectionStatus = "Disconnected"
            )
            is Error -> ConnectionUiState(
                serverUrl = serverUrl,
                token = token,
                error = message,
                connectionStatus = "Error"
            )
        }
    }
}
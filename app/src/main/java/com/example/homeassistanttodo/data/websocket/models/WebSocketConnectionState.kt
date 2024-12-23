package com.example.homeassistanttodo.data.websocket.models

import com.example.homeassistanttodo.ui.connection.ConnectionUiState

sealed class WebSocketConnectionState {
    object Disconnected : WebSocketConnectionState()
    object Connected : WebSocketConnectionState()
    object Authenticated : WebSocketConnectionState()
    data class Error(val message: String) : WebSocketConnectionState()
    data class Reconnecting(val attempt: Int) : WebSocketConnectionState()

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
            is Reconnecting -> ConnectionUiState(
                serverUrl = serverUrl,
                token = token,
                connectionStatus = "Reconnecting attempt $attempt"
            )
        }
    }
}
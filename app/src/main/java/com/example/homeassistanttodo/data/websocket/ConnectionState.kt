package com.example.homeassistanttodo.data.websocket

sealed class ConnectionState {
    object Disconnected : ConnectionState()
    object Connected : ConnectionState()
    object Authenticated : ConnectionState()
    data class Error(val message: String) : ConnectionState()
}
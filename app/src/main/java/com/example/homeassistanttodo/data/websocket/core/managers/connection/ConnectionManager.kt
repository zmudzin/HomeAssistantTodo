package com.example.homeassistanttodo.data.websocket.core.managers.connection

import com.example.homeassistanttodo.data.websocket.models.WebSocketConnectionState
import kotlinx.coroutines.flow.StateFlow

interface ConnectionManager {
    val connectionState: StateFlow<WebSocketConnectionState>
    suspend fun connect(serverUrl: String, apiToken: String)
    suspend fun disconnect()
    fun handleReconnect(serverUrl: String, apiToken: String)
    fun sendMessage(message: String)
    fun setCallbacks(
        onMessage: (String) -> Unit,
        onConnected: () -> Unit
    )
}
package com.example.homeassistanttodo.data.websocket.interfaces

import com.example.homeassistanttodo.data.websocket.models.WebSocketConnectionState
import com.google.gson.JsonElement
import kotlinx.coroutines.flow.StateFlow

interface BasicWebSocketOperations {
    val connectionState: StateFlow<WebSocketConnectionState>
    
    suspend fun connect(serverUrl: String, apiToken: String)
    suspend fun disconnect()
    suspend fun sendCommand(command: String, parameters: Map<String, Any>? = null): Result<JsonElement?>
    suspend fun callService(domain: String, service: String, data: Map<String, Any>? = null): Result<JsonElement?>
}
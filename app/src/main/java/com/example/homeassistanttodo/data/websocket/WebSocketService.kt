package com.example.homeassistanttodo.data.websocket

import com.example.homeassistanttodo.data.websocket.models.WebSocketConnectionState
import com.google.gson.JsonElement
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface WebSocketService {
    val connectionState: StateFlow<WebSocketConnectionState>
    val events: SharedFlow<WebSocketMessage.Event>

    suspend fun connect(serverUrl: String, apiToken: String)
    suspend fun disconnect()
    suspend fun sendCommand(command: String, parameters: Map<String, Any>? = null): Result<JsonElement?>
    suspend fun subscribeToEvents(eventType: String?): Result<Int>
    suspend fun getShoppingListItems(): Result<List<String>>
}
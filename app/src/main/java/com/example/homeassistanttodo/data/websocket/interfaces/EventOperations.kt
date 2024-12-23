package com.example.homeassistanttodo.data.websocket.interfaces

import com.example.homeassistanttodo.data.websocket.WebSocketMessage
import kotlinx.coroutines.flow.SharedFlow

interface EventOperations {
    val events: SharedFlow<WebSocketMessage.Event>
    suspend fun subscribeToEvents(eventType: String?): Result<Int>
    fun registerEventCallback(key: String, callback: (WebSocketMessage.Event) -> Unit)
    fun unregisterEventCallback(key: String)
}
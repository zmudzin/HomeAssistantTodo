package com.example.homeassistanttodo.data.websocket

import com.example.homeassistanttodo.data.model.TodoItem
import com.example.homeassistanttodo.data.websocket.models.WebSocketConnectionState
import com.google.gson.JsonElement
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow

interface WebSocketService {
    val connectionState: StateFlow<WebSocketConnectionState>
    val events: SharedFlow<WebSocketMessage.Event>

    suspend fun connect(serverUrl: String, apiToken: String)
    suspend fun disconnect()
    
    // Todo operations
    suspend fun getTodoItems(entityId: String): Result<List<TodoItem>>
    suspend fun createTodoItem(entityId: String, summary: String): Result<TodoItem>
    suspend fun updateTodoItemStatus(entityId: String, uid: String, status: String): Result<TodoItem>
    suspend fun deleteTodoItem(entityId: String, uid: String): Result<Unit>
    suspend fun subscribeTodoChanges(entityId: String): Result<Int>

    // Legacy operations - do usunięcia po pełnej migracji
    suspend fun getShoppingListItems(): Result<List<String>>
    
    // Base operations
    suspend fun subscribeToEvents(eventType: String?): Result<Int>
    suspend fun sendCommand(command: String, parameters: Map<String, Any>?): Result<JsonElement?>
    suspend fun callService(domain: String, service: String, data: Map<String, Any>?): Result<JsonElement?>
    fun registerEventCallback(key: String, callback: (WebSocketMessage.Event) -> Unit)
    fun unregisterEventCallback(key: String)
}
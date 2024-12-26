package com.example.homeassistanttodo.data.websocket.core

import com.example.homeassistanttodo.data.model.TodoItem
import com.example.homeassistanttodo.data.websocket.WebSocketMessage
import com.example.homeassistanttodo.data.websocket.WebSocketService
import com.example.homeassistanttodo.data.websocket.commands.*
import com.example.homeassistanttodo.data.websocket.core.managers.connection.ConnectionManager
import com.example.homeassistanttodo.data.websocket.core.managers.message.MessageManager
import com.example.homeassistanttodo.data.websocket.core.managers.ping.PingPongManager
import com.example.homeassistanttodo.data.websocket.mapper.TodoResponseMapper
import com.example.homeassistanttodo.data.websocket.models.WebSocketConnectionState
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class HomeAssistantWebSocket @Inject constructor(
    private val connectionManager: ConnectionManager,
    private val messageManager: MessageManager,
    private val pingPongManager: PingPongManager,
    private val gson: Gson,
    private val scope: CoroutineScope
) : WebSocketService {

    private var messageId = 1
    private var currentApiToken: String? = null
    private val eventCallbacks = WebSocketCallback<WebSocketMessage.Event>()

    override val connectionState: StateFlow<WebSocketConnectionState> = connectionManager.connectionState
    override val events: SharedFlow<WebSocketMessage.Event> = messageManager.events

    init {
        connectionManager.setCallbacks(
            onMessage = { message -> 
                scope.launch { 
                    currentApiToken?.let { token ->
                        messageManager.handleMessage(message, token)
                    }
                }
            },
            onConnected = { pingPongManager.startPingPong() }
        )
    }

    override suspend fun connect(serverUrl: String, apiToken: String) {
        currentApiToken = apiToken
        messageManager.startMessageProcessing()
        connectionManager.connect(serverUrl, apiToken)
    }

    override suspend fun disconnect() {
        currentApiToken = null
        pingPongManager.stopPingPong()
        messageManager.stopMessageProcessing()
        connectionManager.disconnect()
    }

    override suspend fun getShoppingListItems(): Result<List<String>> {
        return getTodoItems("todo.lista_zakupow").map { items ->
            items.map { it.summary }
        }
    }

    override suspend fun getTodoItems(entityId: String): Result<List<TodoItem>> {
        val result = messageManager.executeCommand(GetTodoListCommand(messageId++, entityId))
        return result.map { jsonElement -> 
            TodoResponseMapper.mapTodoItems(jsonElement, entityId)
        }
    }

    override suspend fun createTodoItem(entityId: String, summary: String): Result<TodoItem> {
        // 1. Utwórz nowe zadanie
        messageManager.executeCommand(
            CreateTodoItemCommand(
                messageId++,
                entityId,
                summary,
                null,
                null
            )
        ).getOrThrow() // Upewnij się, że zadanie zostało utworzone

        // 2. Pobierz aktualną listę zadań
        val todoListResult = getTodoItems(entityId)
        return todoListResult.map { items ->
            // 3. Znajdź nowo utworzone zadanie (ostatnie z podanym summary)
            items.findLast { it.summary == summary }
                ?: throw IllegalStateException("Failed to create todo item")
        }
    }

    override suspend fun updateTodoItemStatus(
        entityId: String,
        uid: String,
        status: String,
        description: String?,
        due: String?
    ): Result<TodoItem> {
        val result = messageManager.executeCommand(
            UpdateTodoItemCommand(
                messageId++,
                entityId,
                uid,
                status
            )
        )
        // Po aktualizacji statusu, pobierz aktualną listę zadań
        return result.map {
            getTodoItems(entityId).getOrThrow()
                .find { it.uid == uid }
                ?: throw IllegalStateException("Failed to update todo item")
        }
    }

    override suspend fun updateTodoItem(
        entityId: String,
        uid: String,
        summary: String?,
        status: String?,
        description: String?,
        due: String?
    ): Result<TodoItem> {
        // 1. Pobierz obecne zadanie
        val currentItem = getTodoItems(entityId)
            .getOrNull()
            ?.firstOrNull { it.uid == uid }
            ?: throw IllegalStateException("Nie znaleziono zadania")

        // 2. Utwórz nowe zadanie z zaktualizowanymi danymi
        return createTodoItem(
            entityId = entityId,
            summary = summary ?: currentItem.summary
        )
    }

    override suspend fun deleteTodoItem(entityId: String, uid: String): Result<Unit> {
        return messageManager.executeCommand(DeleteTodoItemCommand(messageId++, entityId, uid))
            .map { }
    }

    override suspend fun subscribeTodoChanges(entityId: String): Result<Int> {
        return subscribeToEvents("state_changed")
            .map { subscriptionId ->
                subscriptionId
            }
    }

    override suspend fun subscribeToEvents(eventType: String?): Result<Int> {
        val result = messageManager.executeCommand(SubscribeEventsCommand(messageId++, eventType))
        return result.map { messageId - 1 }
    }

    override suspend fun sendCommand(command: String, parameters: Map<String, Any>?): Result<JsonElement?> =
        messageManager.executeCommand(GenericCommand(messageId++, command, parameters))

    override suspend fun callService(domain: String, service: String, data: Map<String, Any>?): Result<JsonElement?> =
        messageManager.executeCommand(CallServiceCommand(messageId++, domain, service, data))

    override fun registerEventCallback(key: String, callback: (WebSocketMessage.Event) -> Unit) {
        eventCallbacks.register(key, callback)
    }

    override fun unregisterEventCallback(key: String) {
        eventCallbacks.unregister(key)
    }
}
package com.example.homeassistanttodo.data.websocket.core

import com.example.homeassistanttodo.data.websocket.WebSocketMessage
import com.example.homeassistanttodo.data.websocket.WebSocketService
import com.example.homeassistanttodo.data.websocket.commands.*
import com.example.homeassistanttodo.data.websocket.core.managers.connection.ConnectionManager
import com.example.homeassistanttodo.data.websocket.core.managers.message.MessageManager
import com.example.homeassistanttodo.data.websocket.core.managers.ping.PingPongManager
import com.example.homeassistanttodo.data.websocket.models.WebSocketConnectionState
import com.google.gson.Gson
import com.google.gson.JsonElement
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
        val result = messageManager.executeCommand(GetShoppingListCommand(messageId++))
        return result.map { jsonElement ->
            jsonElement?.asJsonObject
                ?.get("response")
                ?.asJsonObject
                ?.get("todo.lista_zakupow")
                ?.asJsonObject
                ?.get("items")
                ?.asJsonArray
                ?.mapNotNull { it.asJsonObject.get("summary")?.asString }
                ?: emptyList()
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
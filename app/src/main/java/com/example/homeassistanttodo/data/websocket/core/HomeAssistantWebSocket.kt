package com.example.homeassistanttodo.data.websocket.core

import android.util.Log
import com.example.homeassistanttodo.data.websocket.commands.*
import com.example.homeassistanttodo.data.websocket.models.WebSocketConnectionState
import com.example.homeassistanttodo.data.websocket.WebSocketMessage
import com.example.homeassistanttodo.data.websocket.WebSocketService
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.math.min

@Singleton
class HomeAssistantWebSocket @Inject constructor(
    private val gson: Gson,
    private val scope: CoroutineScope
) : WebSocketService {
    private var wsClient: WebSocketClient? = null
    private var messageId = 1
    private var reconnectAttempt = 0
    private var pingJob: Job? = null

    private val _connectionState = MutableStateFlow<WebSocketConnectionState>(WebSocketConnectionState.Disconnected)
    override val connectionState: StateFlow<WebSocketConnectionState> = _connectionState

    private val _events = MutableSharedFlow<WebSocketMessage.Event>()
    override val events = _events.asSharedFlow()

    private val pendingCommands = mutableMapOf<Int, CompletableDeferred<WebSocketMessage.Result>>()
    private val eventCallbacks = WebSocketCallback<WebSocketMessage.Event>()

    override suspend fun connect(serverUrl: String, apiToken: String) {
        disconnect()
        reconnectAttempt = 0
        initializeWebSocket(serverUrl, apiToken)
    }

    private inline fun <reified T> handleCommandResponse(response: WebSocketMessage.Result): Result<T> {
        return if (response.success) {
            @Suppress("UNCHECKED_CAST")
            when (T::class) {
                List::class -> Result.success(parseShoppingListResponse(response.result) as T)
                Int::class -> Result.success(response.id as T)
                JsonElement::class -> Result.success(response.result as T)
                else -> Result.success(Unit as T)
            }
        } else {
            Result.failure(Exception(response.error?.get("message")?.asString ?: "Unknown error"))
        }
    }

    private suspend inline fun <reified T> executeCommand(command: Command): Result<T> {
        return try {
            val deferred = CompletableDeferred<WebSocketMessage.Result>()
            if (command.id != 0) {
                pendingCommands[command.id] = deferred
            }

            wsClient?.send(gson.toJson(command.toJson()))

            if (command.id == 0) {
                @Suppress("UNCHECKED_CAST")
                return Result.success(Unit as T)
            }

            val response = withContext(scope.coroutineContext) {
                withTimeout(COMMAND_TIMEOUT) {
                    deferred.await()
                }
            }

            handleCommandResponse(response)
        } catch (e: Exception) {
            Result.failure(e)
        } finally {
            if (command.id != 0) {
                pendingCommands.remove(command.id)
            }
        }
    }

    private fun parseShoppingListResponse(jsonElement: JsonElement?): List<String> {
        return jsonElement?.asJsonObject
            ?.get("response")
            ?.asJsonObject
            ?.get("todo.lista_zakupow")
            ?.asJsonObject
            ?.get("items")
            ?.asJsonArray
            ?.mapNotNull { it.asJsonObject.get("summary")?.asString }
            ?: emptyList()
    }

    // Command methods
    override suspend fun getShoppingListItems(): Result<List<String>> =
        executeCommand(GetShoppingListCommand(messageId++))

    override suspend fun subscribeToEvents(eventType: String?): Result<Int> =
        executeCommand(SubscribeEventsCommand(messageId++, eventType))

    override suspend fun sendCommand(command: String, parameters: Map<String, Any>?): Result<JsonElement?> =
        executeCommand(GenericCommand(messageId++, command, parameters))

    suspend fun callService(domain: String, service: String, data: Map<String, Any>? = null): Result<JsonElement?> =
        executeCommand(CallServiceCommand(messageId++, domain, service, data))

    private fun sendPing() {
        scope.launch {
            executeCommand<Unit>(PingCommand(messageId++))
        }
    }private fun initializeWebSocket(serverUrl: String, apiToken: String) {
        val wsUri = URI(serverUrl)

        wsClient = object : WebSocketClient(wsUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(TAG, "WebSocket opened")
                updateConnectionState(WebSocketConnectionState.Connected)
                startPingPong()
            }

            override fun onMessage(message: String) {
                scope.launch {
                    handleMessage(message, apiToken)
                }
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                Log.d(TAG, "WebSocket closed: $reason")
                stopPingPong()
                updateConnectionState(WebSocketConnectionState.Disconnected)
                handleReconnect(serverUrl, apiToken)
            }

            override fun onError(ex: Exception) {
                Log.e(TAG, "WebSocket error", ex)
                updateConnectionState(WebSocketConnectionState.Error(ex.message ?: "Unknown error"))
            }
        }

        wsClient?.connect()
    }

    private fun updateConnectionState(newState: WebSocketConnectionState) {
        _connectionState.value = newState
        scope.launch {
            when (newState) {
                is WebSocketConnectionState.Connected -> {
                    resubscribeToEvents()
                }
                is WebSocketConnectionState.Error -> {
                    handleError(newState.message)
                }
                else -> {}
            }
        }
    }

    private fun handleError(message: String) {
        Log.e(TAG, "WebSocket error: $message")
        scope.launch {
            pendingCommands.forEach { (_, deferred) ->
                deferred.completeExceptionally(Exception(message))
            }
            pendingCommands.clear()
        }
    }

    private suspend fun resubscribeToEvents() {
        // Implementation of event resubscription
    }

    private fun handleReconnect(serverUrl: String, apiToken: String) {
        if (reconnectAttempt < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempt++
            updateConnectionState(WebSocketConnectionState.Reconnecting(reconnectAttempt))
            val delay = min(INITIAL_RECONNECT_DELAY * reconnectAttempt, MAX_RECONNECT_DELAY)
            scope.launch {
                delay(delay)
                initializeWebSocket(serverUrl, apiToken)
            }
        }
    }

    private fun startPingPong() {
        pingJob?.cancel()
        pingJob = scope.launch {
            while (isActive) {
                delay(PING_INTERVAL)
                if (_connectionState.value is WebSocketConnectionState.Authenticated) {
                    sendPing()
                }
            }
        }
    }

    private fun stopPingPong() {
        pingJob?.cancel()
        pingJob = null
    }

    private suspend fun handleMessage(message: String, apiToken: String) {
        try {
            val jsonMessage = gson.fromJson(message, JsonObject::class.java)
            when (val type = jsonMessage.get("type").asString) {
                "auth_required" -> {
                    executeCommand<Unit>(AuthCommand(apiToken))
                }
                "auth_ok" -> {
                    updateConnectionState(WebSocketConnectionState.Authenticated)
                    reconnectAttempt = 0
                }
                "auth_invalid" -> {
                    updateConnectionState(WebSocketConnectionState.Error("Authentication failed"))
                    disconnect()
                }
                "result" -> {
                    val id = jsonMessage.get("id").asInt
                    pendingCommands[id]?.complete(gson.fromJson(jsonMessage, WebSocketMessage.Result::class.java))
                }
                "event" -> {
                    val event = gson.fromJson(jsonMessage, WebSocketMessage.Event::class.java)
                    _events.emit(event)
                    eventCallbacks.notify(event)
                }
                "pong" -> handlePong(jsonMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling message", e)
        }
    }

    private fun handlePong(message: JsonObject) {
        Log.d(TAG, "Received pong")
    }

    override suspend fun disconnect() {
        stopPingPong()
        wsClient?.close()
        wsClient = null
        updateConnectionState(WebSocketConnectionState.Disconnected)
        pendingCommands.forEach { (_, deferred) ->
            deferred.cancel()
        }
        pendingCommands.clear()
    }

    fun registerEventCallback(key: String, callback: (WebSocketMessage.Event) -> Unit) {
        eventCallbacks.register(key, callback)
    }

    fun unregisterEventCallback(key: String) {
        eventCallbacks.unregister(key)
    }

    companion object {
        private const val TAG = "HomeAssistantWebSocket"
        private const val INITIAL_RECONNECT_DELAY = 1000L // 1 second
        private const val MAX_RECONNECT_DELAY = 30000L // 30 seconds
        private const val MAX_RECONNECT_ATTEMPTS = 5
        private const val PING_INTERVAL = 30000L // 30 seconds
        private const val COMMAND_TIMEOUT = 10000L // 10 seconds
    }
}

class WebSocketCallback<T> {
    private val callbacks = mutableMapOf<String, (T) -> Unit>()

    fun register(key: String, callback: (T) -> Unit) {
        callbacks[key] = callback
    }

    fun unregister(key: String) {
        callbacks.remove(key)
    }

    fun notify(data: T) {
        callbacks.values.forEach { it(data) }
    }
}
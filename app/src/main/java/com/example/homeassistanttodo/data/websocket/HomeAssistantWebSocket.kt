package com.example.homeassistanttodo.data.websocket

import android.util.Log
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
) {
    private var wsClient: WebSocketClient? = null
    private var messageId = 1
    private var reconnectAttempt = 0
    private var pingJob: Job? = null

    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    private val _events = MutableSharedFlow<WebSocketMessage.Event>()
    val events = _events.asSharedFlow()

    private val pendingCommands = mutableMapOf<Int, CompletableDeferred<WebSocketMessage.Result>>()

    fun connect(serverUrl: String, apiToken: String) {
        disconnect()
        reconnectAttempt = 0
        initializeWebSocket(serverUrl, apiToken)
    }

    private fun initializeWebSocket(serverUrl: String, apiToken: String) {
        val wsUri = URI(serverUrl)

        wsClient = object : WebSocketClient(wsUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(TAG, "WebSocket opened")
                _connectionState.value = ConnectionState.Connected
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
                _connectionState.value = ConnectionState.Disconnected
                handleReconnect(serverUrl, apiToken)
            }

            override fun onError(ex: Exception) {
                Log.e(TAG, "WebSocket error", ex)
                _connectionState.value = ConnectionState.Error(ex.message ?: "Unknown error")
            }
        }

        wsClient?.connect()
    }

    private fun handleReconnect(serverUrl: String, apiToken: String) {
        if (reconnectAttempt < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempt++
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
                if (_connectionState.value is ConnectionState.Authenticated) {
                    sendPing()
                }
            }
        }
    }

    private fun stopPingPong() {
        pingJob?.cancel()
        pingJob = null
    }

    suspend fun subscribeToEvents(eventType: String? = null): Result<Int> {
        val id = messageId++
        val message = mapOf(
            "id" to id,
            "type" to "subscribe_events",
            "event_type" to eventType
        )

        return try {
            val response = sendCommandAndWaitForResult(message)
            if (response.success) {
                Result.success(id)
            } else {
                Result.failure(Exception(response.error?.get("message")?.asString ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun callService(domain: String, service: String, data: Map<String, Any>? = null): Result<JsonElement?> {
        val message = mapOf(
            "id" to messageId++,
            "type" to "call_service",
            "domain" to domain,
            "service" to service,
            "service_data" to data
        )

        return try {
            val response = sendCommandAndWaitForResult(message)
            if (response.success) {
                Result.success(response.result)
            } else {
                Result.failure(Exception(response.error?.get("message")?.asString ?: "Unknown error"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun sendCommandAndWaitForResult(message: Map<String, Any?>): WebSocketMessage.Result {
        val deferred = CompletableDeferred<WebSocketMessage.Result>()
        val id = message["id"] as Int
        pendingCommands[id] = deferred

        wsClient?.send(gson.toJson(message))

        return try {
            withTimeout(COMMAND_TIMEOUT) {
                deferred.await()
            }
        } finally {
            pendingCommands.remove(id)
        }
    }

    private suspend fun handleMessage(message: String, apiToken: String) {
        try {
            val jsonMessage = gson.fromJson(message, JsonObject::class.java)
            when (val type = jsonMessage.get("type").asString) {
                "auth_required" -> {
                    val auth = mapOf(
                        "type" to "auth",
                        "access_token" to apiToken
                    )
                    wsClient?.send(gson.toJson(auth))
                }
                "auth_ok" -> {
                    _connectionState.value = ConnectionState.Authenticated
                    reconnectAttempt = 0
                }
                "auth_invalid" -> {
                    _connectionState.value = ConnectionState.Error("Authentication failed")
                    disconnect()
                }
                "result" -> {
                    val id = jsonMessage.get("id").asInt
                    pendingCommands[id]?.complete(gson.fromJson(jsonMessage, WebSocketMessage.Result::class.java))
                }
                "event" -> {
                    val event = gson.fromJson(jsonMessage, WebSocketMessage.Event::class.java)
                    _events.emit(event)
                }
                "pong" -> handlePong(jsonMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling message", e)
        }
    }

    private fun sendPing() {
        val pingMessage = mapOf(
            "id" to messageId++,
            "type" to "ping"
        )
        wsClient?.send(gson.toJson(pingMessage))
    }

    private fun handlePong(message: JsonObject) {
        Log.d(TAG, "Received pong")
    }

    fun disconnect() {
        stopPingPong()
        wsClient?.close()
        wsClient = null
        _connectionState.value = ConnectionState.Disconnected
        pendingCommands.forEach { (_, deferred) ->
            deferred.cancel()
        }
        pendingCommands.clear()
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
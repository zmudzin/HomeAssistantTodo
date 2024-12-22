package com.example.homeassistanttodo.data.websocket

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.inject.Inject
import javax.inject.Singleton

private const val TAG = "HomeAssistantWebSocket"

@Singleton
class HomeAssistantWebSocket @Inject constructor(
    private val gson: Gson
) {
    private var wsClient: WebSocketClient? = null
    private var messageId = 1
    private val _connectionState = MutableStateFlow<ConnectionState>(ConnectionState.Disconnected)
    val connectionState: StateFlow<ConnectionState> = _connectionState

    fun connect(serverUrl: String, apiToken: String) {
        Log.d(TAG, "Connecting to: $serverUrl")
        val wsUri = URI(serverUrl)

        wsClient = object : WebSocketClient(wsUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(TAG, "WebSocket opened")
                _connectionState.value = ConnectionState.Connected
            }

            override fun onMessage(message: String) {
                Log.d(TAG, "Received message: $message")
                handleMessage(message, apiToken)
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                Log.d(TAG, "WebSocket closed: $reason")
                _connectionState.value = ConnectionState.Disconnected
            }

            override fun onError(ex: Exception) {
                Log.e(TAG, "WebSocket error", ex)
                _connectionState.value = ConnectionState.Error(ex.message ?: "Unknown error")
            }
        }
        wsClient?.connect()
    }

    private fun handleMessage(message: String, apiToken: String) {
        try {
            val jsonMessage = gson.fromJson(message, JsonObject::class.java)
            when (jsonMessage.get("type").asString) {
                "auth_required" -> {
                    Log.d(TAG, "Authentication required")
                    authenticate(apiToken)
                }
                "auth_ok" -> {
                    Log.d(TAG, "Authentication successful")
                    _connectionState.value = ConnectionState.Authenticated
                }
                "auth_invalid" -> {
                    Log.e(TAG, "Authentication failed")
                    _connectionState.value = ConnectionState.Error("Authentication failed")
                }
                "result" -> handleResult(jsonMessage)
                "event" -> handleEvent(jsonMessage)
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error handling message", e)
            _connectionState.value = ConnectionState.Error("Failed to parse message: ${e.message}")
        }
    }

    private fun authenticate(apiToken: String) {
        val auth = mapOf(
            "type" to "auth",
            "access_token" to apiToken
        )
        wsClient?.send(gson.toJson(auth))
    }

    private fun handleResult(message: JsonObject) {
        Log.d(TAG, "Result received: ${message}")
    }

    private fun handleEvent(message: JsonObject) {
        val type = message.get("event")?.asJsonObject?.get("event_type")?.asString
        Log.d(TAG, "Event received: $type")
    }

    fun disconnect() {
        wsClient?.close()
        wsClient = null
        _connectionState.value = ConnectionState.Disconnected
    }
}
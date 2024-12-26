package com.example.homeassistanttodo.data.websocket.core.managers.connection

import android.util.Log
import com.example.homeassistanttodo.data.websocket.models.WebSocketConnectionState
import com.example.homeassistanttodo.data.websocket.core.managers.message.CommandIdManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import org.java_websocket.client.WebSocketClient
import org.java_websocket.handshake.ServerHandshake
import java.net.URI
import javax.inject.Inject
import kotlin.math.min

class DefaultConnectionManager @Inject constructor(
    private val scope: CoroutineScope,
    private val commandIdManager: CommandIdManager
) : ConnectionManager {
    companion object {
        private const val TAG = "DefaultConnectionManager"
        private const val INITIAL_RECONNECT_DELAY = 1000L
        private const val MAX_RECONNECT_DELAY = 30000L
        private const val MAX_RECONNECT_ATTEMPTS = 5
    }

    private var wsClient: WebSocketClient? = null
    private var reconnectAttempt = 0
    private var currentApiToken: String? = null
    private val _connectionState = MutableStateFlow<WebSocketConnectionState>(WebSocketConnectionState.Disconnected)
    override val connectionState: StateFlow<WebSocketConnectionState> = _connectionState

    private var onMessageCallback: ((String) -> Unit)? = null
    private var onConnectedCallback: (() -> Unit)? = null

    override fun setCallbacks(
        onMessage: (String) -> Unit,
        onConnected: () -> Unit
    ) {
        onMessageCallback = onMessage
        onConnectedCallback = onConnected
    }

    override suspend fun connect(serverUrl: String, apiToken: String) {
        disconnect()
        reconnectAttempt = 0
        currentApiToken = apiToken
        commandIdManager.reset()  // Reset command IDs on new connection
        initializeWebSocket(serverUrl)
    }

    override suspend fun disconnect() {
        wsClient?.close()
        wsClient = null
        currentApiToken = null
        commandIdManager.reset()  // Reset command IDs on disconnect
        updateConnectionState(WebSocketConnectionState.Disconnected)
    }

    override fun handleReconnect(serverUrl: String, apiToken: String) {
        if (reconnectAttempt < MAX_RECONNECT_ATTEMPTS) {
            reconnectAttempt++
            updateConnectionState(WebSocketConnectionState.Reconnecting(reconnectAttempt))
            val delay = min(INITIAL_RECONNECT_DELAY * reconnectAttempt, MAX_RECONNECT_DELAY)
            scope.launch {
                kotlinx.coroutines.delay(delay)
                connect(serverUrl, apiToken)
            }
        } else {
            updateConnectionState(WebSocketConnectionState.Error("Max reconnection attempts reached"))
        }
    }

    override fun sendMessage(message: String) {
        try {
            wsClient?.send(message)
        } catch (e: Exception) {
            Log.e(TAG, "Error sending message", e)
            throw e
        }
    }

    private fun initializeWebSocket(serverUrl: String) {
        val wsUri = URI(serverUrl)
        wsClient = object : WebSocketClient(wsUri) {
            override fun onOpen(handshakedata: ServerHandshake?) {
                Log.d(TAG, "WebSocket opened")
                updateConnectionState(WebSocketConnectionState.Connected)
                onConnectedCallback?.invoke()
            }

            override fun onMessage(message: String) {
                onMessageCallback?.invoke(message)
            }

            override fun onClose(code: Int, reason: String, remote: Boolean) {
                Log.d(TAG, "WebSocket closed: $reason")
                updateConnectionState(WebSocketConnectionState.Disconnected)
                currentApiToken?.let { token ->
                    handleReconnect(serverUrl, token)
                }
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
        Log.d(TAG, "Connection state updated to: $newState")
    }
}
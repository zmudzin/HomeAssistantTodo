package com.example.homeassistanttodo.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeassistanttodo.BuildConfig
import com.example.homeassistanttodo.data.websocket.HomeAssistantWebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import com.example.homeassistanttodo.data.websocket.ConnectionState
import kotlinx.coroutines.flow.SharingStarted
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val webSocket: HomeAssistantWebSocket
) : ViewModel() {

    val uiState = webSocket.connectionState.map { connectionState ->
        when (connectionState) {
            is ConnectionState.Connected -> ConnectionUiState(
                serverUrl = BuildConfig.HA_SERVER_URL,
                token = BuildConfig.HA_TOKEN,
                connectionStatus = "Connected"
            )
            is ConnectionState.Authenticated -> ConnectionUiState(
                serverUrl = BuildConfig.HA_SERVER_URL,
                token = BuildConfig.HA_TOKEN,
                connectionStatus = "Authenticated"
            )
            is ConnectionState.Disconnected -> ConnectionUiState(
                serverUrl = BuildConfig.HA_SERVER_URL,
                token = BuildConfig.HA_TOKEN,
                connectionStatus = "Disconnected"
            )
            is ConnectionState.Error -> ConnectionUiState(
                serverUrl = BuildConfig.HA_SERVER_URL,
                token = BuildConfig.HA_TOKEN,
                error = connectionState.message,
                connectionStatus = "Error"
            )
        }
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ConnectionUiState(
            serverUrl = BuildConfig.HA_SERVER_URL,
            token = BuildConfig.HA_TOKEN,
            isLoading = true
        )
    )

    init {
        webSocket.connect(BuildConfig.HA_SERVER_URL, BuildConfig.HA_TOKEN)
    }

    override fun onCleared() {
        super.onCleared()
        webSocket.disconnect()
    }
}
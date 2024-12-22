package com.example.homeassistanttodo.ui.connection

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeassistanttodo.BuildConfig
import com.example.homeassistanttodo.data.websocket.HomeAssistantWebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ConnectionViewModel @Inject constructor(
    private val webSocket: HomeAssistantWebSocket
) : ViewModel() {

    val uiState = webSocket.connectionState
        .map { state ->
            state.toUiState(
                serverUrl = BuildConfig.HA_SERVER_URL,
                token = BuildConfig.HA_TOKEN
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = ConnectionUiState.initial(
                serverUrl = BuildConfig.HA_SERVER_URL,
                token = BuildConfig.HA_TOKEN
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
package com.example.homeassistanttodo.ui.connection

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeassistanttodo.BuildConfig
import com.example.homeassistanttodo.data.websocket.core.HomeAssistantWebSocket
import com.example.homeassistanttodo.data.websocket.models.WebSocketConnectionState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
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
            initialValue = ConnectionUiState.Companion.initial(
                serverUrl = BuildConfig.HA_SERVER_URL,
                token = BuildConfig.HA_TOKEN
            )
        )

    init {
        viewModelScope.launch {
            webSocket.connect(BuildConfig.HA_SERVER_URL, BuildConfig.HA_TOKEN)
        }

        // Monitorowanie stanu połączenia i uruchamianie akcji po nawiązaniu połączenia
        viewModelScope.launch {
            webSocket.connectionState
                .filter { it is WebSocketConnectionState.Connected }
                .collect { state ->
                    // Akcja po nawiązaniu połączenia
                    onConnected()
                }
        }
    }

    private suspend fun onConnected() {
        // Przykładowe akcje po nawiązaniu połączenia
        delay(2000)
        val result = webSocket.getShoppingListItems()
        result.onSuccess { items ->
            Log.d(TAG, "Wysłano zapytanie o todo: $items")
        }.onFailure { error ->
            Log.e(TAG, "Wystąpił błąd", error)
        }

        val subscribeResult = webSocket.subscribeToEvents("state_changed")
        subscribeResult.onSuccess { subscriptionId ->
            Log.d(TAG, "Subskrybuj zmiany stanu. ID: $subscriptionId")
        }.onFailure { error ->
            Log.e(TAG, "Subskrypcja nie powiodła się", error)
        }

        webSocket.registerEventCallback("state_changes") { event ->
            Log.d(TAG, "Zmiana stanu: ${event.event}")
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            webSocket.disconnect()
        }
    }
}

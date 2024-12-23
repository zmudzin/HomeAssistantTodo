package com.example.homeassistanttodo.ui.connection

import android.content.ContentValues.TAG
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeassistanttodo.BuildConfig
import com.example.homeassistanttodo.data.websocket.core.HomeAssistantWebSocket
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
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

//            viewModelScope.launch {
//                delay(4000)
//                val result = webSocket.subscribeToEvents("state_changed")
//                result.onSuccess { subscriptionId ->
//                    Log.d(TAG, "Subscribed to state changes. ID: $subscriptionId")
//                }.onFailure { error ->
//                    Log.e(TAG, "Subscription failed", error)
//                }
//            }
            viewModelScope.launch {
                delay(3000)
                val result = webSocket.getShoppingListItems()
                result.onSuccess { subscriptionId ->
                    Log.d(TAG, "wysłano zapytanie o todo: $subscriptionId")
                }.onFailure { error ->
                    Log.e(TAG, "Skurwiaj", error)
                }
            }
            webSocket.registerEventCallback("state_changes") { event ->
                Log.d(TAG, "State changed: ${event.event}")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        viewModelScope.launch {
            webSocket.disconnect()
        }
    }
}
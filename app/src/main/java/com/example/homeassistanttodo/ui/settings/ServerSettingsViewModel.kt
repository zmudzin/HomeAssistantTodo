package com.example.homeassistanttodo.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeassistanttodo.BuildConfig
import com.example.homeassistanttodo.data.preferences.ServerPreferences
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

data class ServerSettingsUiState(
    val serverUrl: String = "",
    val token: String = "",
    val isLoading: Boolean = true,
    val isSaving: Boolean = false,
    val error: String? = null,
    val isSuccess: Boolean = false
) {
    companion object {
        fun initial() = ServerSettingsUiState(isLoading = true)
    }
}

@HiltViewModel
class ServerSettingsViewModel @Inject constructor(
    private val serverPreferences: ServerPreferences
) : ViewModel() {

    private val _uiState = MutableStateFlow(ServerSettingsUiState.initial())
    val uiState = combine(
        serverPreferences.serverUrl,
        serverPreferences.token,
        _uiState
    ) { serverUrl, token, state ->
        state.copy(
            serverUrl = serverUrl.ifEmpty { BuildConfig.HA_SERVER_URL },
            token = token.ifEmpty { BuildConfig.HA_TOKEN },
            isLoading = false
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5000),
        ServerSettingsUiState.initial()
    )

    fun saveSettings(serverUrl: String, token: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true, error = null) }
            try {
                serverPreferences.saveServerSettings(serverUrl, token)
                _uiState.update { it.copy(isSaving = false, isSuccess = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        error = e.message ?: "Failed to save settings"
                    )
                }
            }
        }
    }

    fun resetSuccess() {
        _uiState.update { it.copy(isSuccess = false) }
    }
}
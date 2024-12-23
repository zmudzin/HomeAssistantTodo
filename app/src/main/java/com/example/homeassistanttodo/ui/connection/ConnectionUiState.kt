package com.example.homeassistanttodo.ui.connection

data class ConnectionUiState(
    val serverUrl: String = "",
    val token: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val connectionStatus: String = "Disconnected"
) {
    // Pomocnicze właściwości dla UI
    val isError: Boolean get() = error != null
    val isConnected: Boolean get() = connectionStatus in listOf("Connected", "Authenticated")

    companion object {
        fun initial(serverUrl: String = "", token: String = "") = ConnectionUiState(
            serverUrl = serverUrl,
            token = token,
            isLoading = true
        )
    }
}

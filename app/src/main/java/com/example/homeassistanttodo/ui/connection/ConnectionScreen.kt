package com.example.homeassistanttodo.ui.connection

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@Composable
fun ConnectionScreen(
    viewModel: ConnectionViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedTextField(
            value = uiState.serverUrl,
            onValueChange = { },
            label = { Text("Server URL") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        OutlinedTextField(
            value = uiState.token,
            onValueChange = { },
            label = { Text("Access Token") },
            modifier = Modifier.fillMaxWidth(),
            readOnly = true
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = when (uiState.connectionStatus) {
                    "Authenticated" -> MaterialTheme.colorScheme.primaryContainer
                    "Error" -> MaterialTheme.colorScheme.errorContainer
                    else -> MaterialTheme.colorScheme.surfaceVariant
                }
            )
        ) {
            Text(
                text = "Status: ${uiState.connectionStatus}",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.titleMedium,
                color = when (uiState.connectionStatus) {
                    "Authenticated" -> MaterialTheme.colorScheme.onPrimaryContainer
                    "Error" -> MaterialTheme.colorScheme.onErrorContainer
                    else -> MaterialTheme.colorScheme.onSurfaceVariant
                }
            )
        }

        if (uiState.isLoading) {
            CircularProgressIndicator()
        }

        uiState.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
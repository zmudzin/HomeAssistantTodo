package com.example.homeassistanttodo.data.sync

import android.util.Log
import com.example.homeassistanttodo.data.controller.TodoListController
import com.example.homeassistanttodo.data.model.TodoItem
import com.example.homeassistanttodo.data.websocket.WebSocketMessage
import com.google.gson.JsonObject
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoSyncService @Inject constructor(
    private val todoListController: TodoListController,
    private val scope: CoroutineScope
) {
    private var syncJob: Job? = null
    private val _syncState = MutableStateFlow<SyncState>(SyncState.Idle)
    val syncState: StateFlow<SyncState> = _syncState

    fun startSync(syncInterval: Long = DEFAULT_SYNC_INTERVAL) {
        stopSync()
        syncJob = scope.launch {
            while (isActive) {
                syncLists()
                delay(syncInterval)
            }
        }
    }

    fun stopSync() {
        syncJob?.cancel()
        syncJob = null
        _syncState.value = SyncState.Idle
    }

    suspend fun syncLists() {
        try {
            _syncState.value = SyncState.Syncing
            
            todoListController.getTodoLists()
                .onSuccess { lists ->
                    lists.forEach { list ->
                        syncList(list.entityId)
                    }
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to sync lists", error)
                    _syncState.value = SyncState.Error(error)
                    return
                }

            _syncState.value = SyncState.Success
        } catch (e: Exception) {
            Log.e(TAG, "Sync error", e)
            _syncState.value = SyncState.Error(e)
        }
    }

    suspend fun syncList(listId: String) {
        try {
            todoListController.getListItems(listId)
                .onSuccess { items ->
                    // TODO: Po implementacji Room:
                    // - Zapisz items do lokalnej bazy
                    // - Porównaj z lokalnymi danymi
                    // - Rozwiąż konflikty
                    Log.d(TAG, "Synced ${items.size} items for list $listId")
                }
                .onFailure { error ->
                    Log.e(TAG, "Failed to sync list $listId", error)
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing list $listId", e)
        }
    }

    /**
     * Obsługuje wydarzenia zmiany stanu z WebSocket
     */
    suspend fun handleStateChange(event: WebSocketMessage.Event) {
        try {
            val data = event.event.getAsJsonObject("data")
            val entityId = data.get("entity_id").asString
            
            // Filtruj tylko wydarzenia todo
            if (!entityId.startsWith("todo.")) return
            
            val newState = data.getAsJsonObject("new_state")
            val items = newState.getAsJsonObject("attributes")
                .getAsJsonArray("items")
                .map { it.asJsonObject }
                .map { mapEventToTodoItem(it, entityId) }

            // TODO: Po implementacji Room:
            // - Zapisz zaktualizowane items do bazy
            // - Rozwiąż konflikty jeśli istnieją lokalne zmiany
            Log.d(TAG, "Received state change for $entityId with ${items.size} items")
        } catch (e: Exception) {
            Log.e(TAG, "Error handling state change", e)
        }
    }

    private fun mapEventToTodoItem(json: JsonObject, listId: String): TodoItem {
        return TodoItem(
            uid = json.get("uid").asString,
            summary = json.get("summary").asString,
            status = json.get("status").asString,
            listId = listId,
            lastChanged = json.get("last_changed")?.asString,
            lastUpdated = json.get("last_updated")?.asString
        )
    }

    sealed class SyncState {
        object Idle : SyncState()
        object Syncing : SyncState()
        object Success : SyncState()
        data class Error(val error: Throwable) : SyncState()
    }

    companion object {
        private const val TAG = "TodoSyncService"
        private const val DEFAULT_SYNC_INTERVAL = 5 * 60 * 1000L // 5 minut
    }
}
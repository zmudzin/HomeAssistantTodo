package com.example.homeassistanttodo.ui.todo

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeassistanttodo.data.model.TodoItem
import com.example.homeassistanttodo.data.websocket.WebSocketService
import com.google.gson.JsonObject
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val webSocketService: WebSocketService
) : ViewModel() {
    private val _todoItems = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoItems = _todoItems.asStateFlow()
    
    // Stan dla dialogów
    private val _showAddDialog = MutableStateFlow(false)
    val showAddDialog = _showAddDialog.asStateFlow()

    private val _editingItem = MutableStateFlow<TodoItem?>(null)
    val editingItem = _editingItem.asStateFlow()
    
    private val entityId = "todo.lista_zakupow"
    private val lastUpdate = MutableStateFlow(System.currentTimeMillis())

    init {
        loadTodoItems()
        observeStateChanges()
    }

    private fun observeStateChanges() {
        viewModelScope.launch {
            webSocketService.subscribeTodoChanges(entityId)
            webSocketService.events
                .distinctUntilChanged()
                .collect { event ->
                    Log.d("TodoViewModel", "Raw WebSocket event received: $event")

                    // Bardziej bezpieczne sprawdzenie zdarzenia
                    try {
                        // Zmiana: używamy event.event zamiast event.data
                        val eventObject = event.event

                        // Sprawdzenie, czy to zdarzenie state_changed
                        val eventType = eventObject.get("event_type")?.asString
                        val data = eventObject.getAsJsonObject("data")
                        val changedEntityId = data?.get("entity_id")?.asString

                        if (eventType == "state_changed" && changedEntityId == entityId) {
                            Log.d("TodoViewModel", "Entity $entityId state changed, reloading items")
                            delay(200)
                            loadTodoItems()
                        }
                    } catch (e: Exception) {
                        Log.e("TodoViewModel", "Error processing WebSocket event", e)
                    }
                }
        }
    }

    private fun loadTodoItems() {
        viewModelScope.launch {
            Log.d("TodoViewModel", "Loading todo items...")
            webSocketService.getTodoItems(entityId)
                .onSuccess { items ->
                    Log.d("TodoViewModel", "Loaded items: ${items.map { it.summary }}")
                    _todoItems.value = items
                    lastUpdate.value = System.currentTimeMillis()
                }
                .onFailure { error ->
                    Log.e("TodoViewModel", "Error loading items: ${error.message}")
                }
        }
    }

    fun createTodoItem(summary: String) {
        viewModelScope.launch {
            Log.d("TodoViewModel", "Starting creation of item with summary: $summary")
            webSocketService.createTodoItem(entityId, summary)
                .onSuccess { response -> 
                    Log.d("TodoViewModel", "Creation response: $response")
                }
                .onFailure { error ->
                    Log.e("TodoViewModel", "Error creating item: ${error.message}")
                }
        }
    }

    fun updateTodoItemSummary(uid: String, newSummary: String) {
        viewModelScope.launch {
            Log.d("TodoViewModel", "Starting update of item: $uid to: $newSummary")
            webSocketService.updateTodoItem(entityId, uid, summary = newSummary)
                .onSuccess { response ->
                    Log.d("TodoViewModel", "Update response: $response")
                }
                .onFailure { error ->
                    Log.e("TodoViewModel", "Error updating item: ${error.message}")
                }
        }
    }

    fun updateItemStatus(uid: String, status: String) {
        viewModelScope.launch {
            Log.d("TodoViewModel", "Starting status update of item: $uid to: $status")
            webSocketService.updateTodoItemStatus(entityId, uid, status)
                .onSuccess { response ->
                    Log.d("TodoViewModel", "Status update response: $response")
                    delay(300)
                    loadTodoItems()  // Dodane
                }
                .onFailure { error ->
                    Log.e("TodoViewModel", "Error updating status: ${error.message}")
                }
        }
    }

    fun deleteItem(uid: String) {
        viewModelScope.launch {
            Log.d("TodoViewModel", "Starting deletion of item: $uid")
            webSocketService.deleteTodoItem(
                entityId,
                uid
            )
                .onSuccess { response ->
                    Log.d("TodoViewModel", "Delete response: $response")
                    delay(300)
                    loadTodoItems()
                }
                .onFailure { error ->
                    Log.e("TodoViewModel", "Error deleting item: ${error.message}")
                }
        }
    }

    fun deleteCompletedItems() {
        viewModelScope.launch {
            val completedItems = _todoItems.value.filter { it.status == "completed" }
            completedItems.forEach { item ->
                deleteItem(item.uid)
            }
        }
    }

    // Kontrola dialogów
    fun showAddDialog() {
        _showAddDialog.value = true
    }

    fun hideAddDialog() {
        _showAddDialog.value = false
    }

    fun showEditDialog(item: TodoItem) {
        _editingItem.value = item
    }

    fun hideEditDialog() {
        _editingItem.value = null
    }
}
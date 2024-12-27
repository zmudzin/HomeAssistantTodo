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
                .distinctUntilChanged { old, new -> 
                    old.event.toString() == new.event.toString()
                }
                .collect { event ->
                    val eventJson = event.event as? JsonObject
                    val eventType = eventJson?.get("event_type")?.asString
                    val data = eventJson?.getAsJsonObject("data")
                    val changedEntityId = data?.get("entity_id")?.asString
                    
                    if (eventType == "state_changed" && changedEntityId == entityId) {
                        Log.d("TodoViewModel", "Entity $entityId state changed, reloading items")
                        delay(100)
                        loadTodoItems()
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
            webSocketService.createTodoItem(entityId, summary)
                .onSuccess { 
                    delay(100)
                    loadTodoItems()
                }
                .onFailure { error ->
                    Log.e("TodoViewModel", "Error creating item: ${error.message}")
                }
        }
    }

    fun updateTodoItemSummary(uid: String, newSummary: String) {
        viewModelScope.launch {
            webSocketService.updateTodoItem(entityId, uid, summary = newSummary)
                .onSuccess { 
                    delay(100)
                    loadTodoItems()
                }
                .onFailure { error ->
                    Log.e("TodoViewModel", "Error updating item: ${error.message}")
                }
        }
    }

    fun updateItemStatus(uid: String, status: String) {
        viewModelScope.launch {
            Log.d("TodoViewModel", "Updating item status: $uid to $status")
            webSocketService.updateTodoItemStatus(entityId, uid, status)
                .onSuccess { updatedItem ->
                    Log.d("TodoViewModel", "Status updated successfully")
                }
                .onFailure { error ->
                    Log.e("TodoViewModel", "Error updating status: ${error.message}")
                }
        }
    }

    fun deleteItem(uid: String) {
        viewModelScope.launch {
            Log.d("TodoViewModel", "Deleting item: $uid")
            webSocketService.deleteTodoItem(
                entityId,     // lista (todo.lista_zakupow)
                uid           // ID zadania
            )
                .onSuccess {
                    Log.d("TodoViewModel", "Item deleted successfully")
                    delay(100)
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
package com.example.homeassistanttodo.ui.todo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.homeassistanttodo.data.model.TodoItem
import com.example.homeassistanttodo.data.websocket.WebSocketService
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TodoViewModel @Inject constructor(
    private val webSocketService: WebSocketService
) : ViewModel() {
    private val _todoItems = MutableStateFlow<List<TodoItem>>(emptyList())
    val todoItems = _todoItems.asStateFlow()
    
    private val entityId = "todo.lista_zakupow"

    init {
        loadTodoItems()
    }

    private fun loadTodoItems() {
        viewModelScope.launch {
            webSocketService.getTodoItems(entityId)
                .onSuccess { items ->
                    _todoItems.value = items
                }
        }
    }

    fun updateItemStatus(uid: String, status: String) {
        viewModelScope.launch {
            webSocketService.updateTodoItemStatus(entityId, uid, status)
                .onSuccess { updatedItem ->
                    loadTodoItems()
                }
        }
    }

    fun deleteItem(uid: String) {
        viewModelScope.launch {
            webSocketService.deleteTodoItem(entityId, uid)
                .onSuccess { 
                    loadTodoItems()
                }
        }
    }
}
package com.example.homeassistanttodo.data.model

import com.example.homeassistanttodo.data.local.entity.TodoItem
import com.example.homeassistanttodo.data.local.entity.TodoItemStatus

// Zakładam strukturę odpowiedzi z WebSocket
data class TodoListResponse(
    val items: List<TodoWebSocketItem>
)

data class TodoWebSocketItem(
    val uid: String,
    val summary: String,
    val status: String, // np. "needs_action" lub "completed"
    val order: Int
) {
    // Metoda konwersji do lokalnej encji
    fun toTodoItem(): TodoItem = TodoItem(
        uid = uid,
        summary = summary,
        status = when(status.lowercase()) {
            "needs_action" -> TodoItemStatus.NEEDS_ACTION
            "completed" -> TodoItemStatus.COMPLETED
            else -> TodoItemStatus.NEEDS_ACTION // domyślnie
        },
        orderPosition = order
    )
}
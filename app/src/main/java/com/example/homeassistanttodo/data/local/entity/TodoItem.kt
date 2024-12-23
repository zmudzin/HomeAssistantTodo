package com.example.homeassistanttodo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.homeassistanttodo.data.model.TodoWebSocketItem

@Entity(tableName = "todo_items")
data class TodoItem(
    @PrimaryKey val uid: String,
    val summary: String,
    val status: TodoItemStatus,
    val orderPosition: Int
) {
    companion object {
        fun fromWebSocketItem(item: TodoWebSocketItem): TodoItem {
            return TodoItem(
                uid = item.uid,
                summary = item.summary,
                status = when (item.status) {
                    "needs_action" -> TodoItemStatus.NEEDS_ACTION
                    "completed" -> TodoItemStatus.COMPLETED
                    else -> TodoItemStatus.NEEDS_ACTION
                },
                orderPosition = item.order
            )
        }
    }
}
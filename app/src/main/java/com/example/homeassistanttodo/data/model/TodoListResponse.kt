package com.example.homeassistanttodo.data.model

import com.example.homeassistanttodo.data.local.entity.TodoItem
import com.example.homeassistanttodo.data.local.entity.TodoItemStatus
import com.google.gson.annotations.SerializedName

data class TodoListResponse(
    val id: Int,
    val type: String,
    val success: Boolean,
    val result: List<TodoItemResponse>
)

data class TodoItemResponse(
    val summary: String,
    val uid: String,
    val status: String,
    @SerializedName("order_position") 
    val orderPosition: Int
) {
    fun toEntity(): TodoItem = TodoItem(
        uid = uid,
        summary = summary,
        status = when (status.uppercase()) {
            "NEEDS_ACTION" -> TodoItemStatus.NEEDS_ACTION
            "COMPLETED" -> TodoItemStatus.COMPLETED
            else -> TodoItemStatus.NEEDS_ACTION
        },
        orderPosition = orderPosition
    )
}

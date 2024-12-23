package com.example.homeassistanttodo.data.local.converter

import androidx.room.TypeConverter
import com.example.homeassistanttodo.data.local.entity.TodoItemStatus

class TodoItemStatusConverter {
    @TypeConverter
    fun fromStatus(status: TodoItemStatus): String {
        return status.name
    }

    @TypeConverter
    fun toStatus(status: String): TodoItemStatus {
        return TodoItemStatus.valueOf(status)
    }
}

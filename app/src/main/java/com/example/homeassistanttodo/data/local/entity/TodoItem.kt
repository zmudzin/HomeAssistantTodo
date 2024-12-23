package com.example.homeassistanttodo.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class TodoItem(
    @PrimaryKey val uid: String,
    val summary: String,
    val status: TodoItemStatus,
    val orderPosition: Int
)

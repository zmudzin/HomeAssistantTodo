package com.example.homeassistanttodo.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.homeassistanttodo.data.local.dao.TodoItemDao
import com.example.homeassistanttodo.data.local.entity.TodoItem
import com.example.homeassistanttodo.data.local.converter.TodoItemStatusConverter

@Database(
    entities = [TodoItem::class],
    version = 1
)
@TypeConverters(TodoItemStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoItemDao(): TodoItemDao
}

package com.example.homeassistanttodo.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.homeassistanttodo.data.local.dao.TodoItemDao
import com.example.homeassistanttodo.data.local.entity.TodoItem
import com.example.homeassistanttodo.data.local.entity.TodoItemStatus


@Database(entities = [TodoItem::class], version = 2)
@TypeConverters(TodoItemStatusConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun todoItemDao(): TodoItemDao

    companion object {
        private var instance: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instance ?: synchronized(this) {
                instance ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "home_assistant_todo_db"
                ).fallbackToDestructiveMigration().build().also { instance = it }
            }
        }
    }
}

class TodoItemStatusConverter {
    @androidx.room.TypeConverter
    fun fromStatus(status: TodoItemStatus): String = status.name

    @androidx.room.TypeConverter
    fun toStatus(statusName: String): TodoItemStatus = 
        TodoItemStatus.valueOf(statusName)
}
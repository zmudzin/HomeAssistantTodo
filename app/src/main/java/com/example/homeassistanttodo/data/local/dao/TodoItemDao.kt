package com.example.homeassistanttodo.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.homeassistanttodo.data.local.entity.TodoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoItemDao {
    @Query("SELECT * FROM todo_items ORDER BY orderPosition")
    fun getAllItems(): Flow<List<TodoItem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TodoItem>)

    @Query("SELECT * FROM todo_items")
    suspend fun getAllItemsSync(): List<TodoItem>
}
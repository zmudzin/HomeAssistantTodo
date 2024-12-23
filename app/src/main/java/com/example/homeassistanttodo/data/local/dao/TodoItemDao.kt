package com.example.homeassistanttodo.data.local.dao

import androidx.room.*
import com.example.homeassistanttodo.data.local.entity.TodoItem
import kotlinx.coroutines.flow.Flow

@Dao
interface TodoItemDao {
    @Query("SELECT * FROM TodoItem ORDER BY orderPosition")
    fun getAllItems(): Flow<List<TodoItem>>
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertItems(items: List<TodoItem>)
    
    @Query("DELETE FROM TodoItem")
    suspend fun deleteAllItems()
    
    @Transaction
    suspend fun replaceAllItems(items: List<TodoItem>) {
        deleteAllItems()
        insertItems(items)
    }
}

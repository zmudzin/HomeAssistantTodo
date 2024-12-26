package com.example.homeassistanttodo.data.repository

import com.example.homeassistanttodo.data.model.TodoItem
import com.example.homeassistanttodo.data.model.TodoList
import kotlinx.coroutines.flow.Flow

interface TodoRepository {
    // Room operations
    suspend fun insertTodoItem(item: TodoItem)
    suspend fun insertTodoItems(items: List<TodoItem>)
    suspend fun updateTodoItem(item: TodoItem)
    suspend fun deleteTodoItem(item: TodoItem)
    suspend fun getTodoItems(listId: String): Flow<List<TodoItem>>
    
    // Lists management
    suspend fun getTodoLists(): Flow<List<TodoList>>
    suspend fun insertTodoList(list: TodoList)
    suspend fun deleteTodoList(list: TodoList)
    
    // WebSocket operations
    suspend fun fetchTodoItems(listId: String): Result<List<TodoItem>>
    suspend fun markItemCompleted(listId: String, itemId: String): Result<Unit>
    suspend fun markItemUncompleted(listId: String, itemId: String): Result<Unit>
    suspend fun createTodoItem(listId: String, summary: String): Result<TodoItem>
    
    // Sync operations
    suspend fun syncTodoList(listId: String)
    suspend fun syncAllLists()
}
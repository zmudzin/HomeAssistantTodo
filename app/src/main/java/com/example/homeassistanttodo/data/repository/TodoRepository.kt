package com.example.homeassistanttodo.data.repository

import com.example.homeassistanttodo.data.local.dao.TodoItemDao
import com.example.homeassistanttodo.data.local.entity.TodoItem
import com.example.homeassistanttodo.data.model.TodoListResponse
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class TodoRepository @Inject constructor(
    private val todoItemDao: TodoItemDao
) {
    val todoItems: Flow<List<TodoItem>> = todoItemDao.getAllItems()

    suspend fun updateTodoItems(response: TodoListResponse) {
        if (response.success) {
            val items = response.result.map { it.toEntity() }
            todoItemDao.replaceAllItems(items)
        }
    }
}

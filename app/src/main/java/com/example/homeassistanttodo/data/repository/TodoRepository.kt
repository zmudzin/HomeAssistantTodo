package com.example.homeassistanttodo.data.repository

import com.example.homeassistanttodo.data.local.dao.TodoItemDao
import com.example.homeassistanttodo.data.model.TodoListResponse
import kotlinx.coroutines.flow.Flow
import com.example.homeassistanttodo.data.local.entity.TodoItem
import javax.inject.Inject

class TodoRepository @Inject constructor(
    private val todoItemDao: TodoItemDao
) {
    suspend fun updateTodoItems(response: TodoListResponse) {
        val existingItems = todoItemDao.getAllItemsSync() // nowa metoda
        val newItems = response.items
            .map { it.toTodoItem() }
            .filterNot { newItem ->
                existingItems.any { it.summary == newItem.summary }
            }
        if (newItems.isNotEmpty()) {
            todoItemDao.insertItems(newItems)
        }
    }
}
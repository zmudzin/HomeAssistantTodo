package com.example.homeassistanttodo.data.controller

import com.example.homeassistanttodo.data.model.TodoItem
import com.example.homeassistanttodo.data.model.TodoList
import com.example.homeassistanttodo.data.websocket.WebSocketService
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TodoListController @Inject constructor(
    private val webSocketService: WebSocketService
) {
    /**
     * Pobiera wszystkie listy z Home Assistant.
     * Na razie zwraca tylko listę zakupów, później będzie rozszerzone o dynamiczne wykrywanie list
     */
    suspend fun getTodoLists(): Result<List<TodoList>> = 
        Result.success(listOf(
            TodoList(
                entityId = "todo.lista_zakupow",
                name = "Lista zakupów",
                domain = "todo"
            )
        ))

    /**
     * Pobiera wszystkie zadania z danej listy
     */
    suspend fun getListItems(listId: String): Result<List<TodoItem>> =
        webSocketService.getTodoItems(listId)

    /**
     * Dodaje nowe zadanie do listy
     */
    suspend fun addItem(listId: String, summary: String): Result<TodoItem> =
        webSocketService.createTodoItem(listId, summary)

    /**
     * Aktualizuje status zadania
     */
    suspend fun updateItemStatus(listId: String, itemId: String, completed: Boolean): Result<TodoItem> {
        val status = if (completed) "completed" else "needs_action"
        return webSocketService.updateTodoItemStatus(listId, itemId, status)
    }

    /**
     * Usuwa zadanie z listy
     */
    suspend fun removeItem(listId: String, itemId: String): Result<Unit> =
        webSocketService.deleteTodoItem(listId, itemId)

    /**
     * Subskrybuje zmiany dla danej listy
     */
    suspend fun subscribeToListChanges(listId: String): Result<Int> =
        webSocketService.subscribeTodoChanges(listId)
}
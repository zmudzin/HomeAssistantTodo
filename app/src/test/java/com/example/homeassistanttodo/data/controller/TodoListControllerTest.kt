package com.example.homeassistanttodo.data.controller

import com.example.homeassistanttodo.data.model.TodoItem
import com.example.homeassistanttodo.data.websocket.WebSocketService
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class TodoListControllerTest {
    private lateinit var webSocketService: WebSocketService
    private lateinit var todoListController: TodoListController

    @Before
    fun setup() {
        webSocketService = mockk()
        todoListController = TodoListController(webSocketService)
    }

    @Test
    fun `getTodoLists returns default shopping list`() = runTest {
        val result = todoListController.getTodoLists()
        
        assert(result.isSuccess)
        result.onSuccess { lists ->
            assertEquals(1, lists.size)
            assertEquals("todo.lista_zakupow", lists[0].entityId)
            assertEquals("Lista zakupów", lists[0].name)
        }
    }

    @Test
    fun `getListItems delegates to webSocketService`() = runTest {
        val listId = "todo.lista_zakupow"
        val mockItems = listOf(
            TodoItem(
                uid = "1",
                summary = "Test item",
                status = "needs_action",
                listId = listId
            )
        )
        
        coEvery { webSocketService.getTodoItems(listId) } returns Result.success(mockItems)

        val result = todoListController.getListItems(listId)
        
        assert(result.isSuccess)
        coVerify { webSocketService.getTodoItems(listId) }
        result.onSuccess { items ->
            assertEquals(mockItems, items)
        }
    }

    @Test
    fun `addItem delegates to webSocketService`() = runTest {
        val listId = "todo.lista_zakupow"
        val summary = "New item"
        val mockItem = TodoItem(
            uid = "1",
            summary = summary,
            status = "needs_action",
            listId = listId
        )
        
        coEvery { webSocketService.createTodoItem(listId, summary) } returns Result.success(mockItem)

        val result = todoListController.addItem(listId, summary)
        
        assert(result.isSuccess)
        coVerify { webSocketService.createTodoItem(listId, summary) }
        result.onSuccess { item ->
            assertEquals(mockItem, item)
        }
    }

    @Test
    fun `updateItemStatus completes item`() = runTest {
        val listId = "todo.lista_zakupow"
        val itemId = "1"
        val mockItem = TodoItem(
            uid = itemId,
            summary = "Test item",
            status = "completed",
            listId = listId
        )
        
        coEvery { 
            webSocketService.updateTodoItemStatus(listId, itemId, "completed") 
        } returns Result.success(mockItem)

        val result = todoListController.updateItemStatus(listId, itemId, true)
        
        assert(result.isSuccess)
        coVerify { webSocketService.updateTodoItemStatus(listId, itemId, "completed") }
        result.onSuccess { item ->
            assertEquals(mockItem, item)
        }
    }

    @Test
    fun `updateItemStatus uncompletes item`() = runTest {
        val listId = "todo.lista_zakupow"
        val itemId = "1"
        val mockItem = TodoItem(
            uid = itemId,
            summary = "Test item",
            status = "needs_action",
            listId = listId
        )
        
        coEvery { 
            webSocketService.updateTodoItemStatus(listId, itemId, "needs_action") 
        } returns Result.success(mockItem)

        val result = todoListController.updateItemStatus(listId, itemId, false)
        
        assert(result.isSuccess)
        coVerify { webSocketService.updateTodoItemStatus(listId, itemId, "needs_action") }
        result.onSuccess { item ->
            assertEquals(mockItem, item)
        }
    }

    @Test
    fun `removeItem delegates to webSocketService`() = runTest {
        val listId = "todo.lista_zakupow"
        val itemId = "1"
        
        coEvery { webSocketService.deleteTodoItem(listId, itemId) } returns Result.success(Unit)

        val result = todoListController.removeItem(listId, itemId)
        
        assert(result.isSuccess)
        coVerify { webSocketService.deleteTodoItem(listId, itemId) }
    }

    @Test
    fun `subscribeToListChanges delegates to webSocketService`() = runTest {
        val listId = "todo.lista_zakupow"
        val mockSubscriptionId = 1
        
        coEvery { 
            webSocketService.subscribeTodoChanges(listId) 
        } returns Result.success(mockSubscriptionId)

        val result = todoListController.subscribeToListChanges(listId)
        
        assert(result.isSuccess)
        coVerify { webSocketService.subscribeTodoChanges(listId) }
        result.onSuccess { subscriptionId ->
            assertEquals(mockSubscriptionId, subscriptionId)
        }
    }

    @Test
    fun `getListItems handles error from webSocketService`() = runTest {
        val listId = "todo.lista_zakupow"
        val error = RuntimeException("Network error")
        
        coEvery { webSocketService.getTodoItems(listId) } returns Result.failure(error)

        val result = todoListController.getListItems(listId)
        
        assert(result.isFailure)
        coVerify { webSocketService.getTodoItems(listId) }
        result.onFailure { exception ->
            assertEquals(error, exception)
        }
    }
}
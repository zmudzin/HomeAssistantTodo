package com.example.homeassistanttodo.data.sync

import com.example.homeassistanttodo.MainCoroutineRule
import com.example.homeassistanttodo.data.controller.TodoListController
import com.example.homeassistanttodo.data.model.TodoItem
import com.example.homeassistanttodo.data.model.TodoList
import com.example.homeassistanttodo.data.websocket.WebSocketMessage
import com.example.homeassistanttodo.mockLogger
import com.google.gson.JsonParser
import io.mockk.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class TodoSyncServiceTest {
    @get:Rule
    val mainCoroutineRule = MainCoroutineRule()

    private lateinit var todoListController: TodoListController
    private lateinit var todoSyncService: TodoSyncService
    private lateinit var testScope: TestScope

    @Before
    fun setup() {
        mockLogger()
        todoListController = mockk(relaxed = true)
        testScope = TestScope()
        todoSyncService = TodoSyncService(todoListController, testScope)
    }

    @Test
    fun `syncLists syncs all available lists`() = runTest {
        // Given
        val mockLists = listOf(
            TodoList("todo.lista_zakupow", "Lista zakupów"),
            TodoList("todo.zadania_domowe", "Zadania domowe")
        )
        val mockItems = listOf(
            TodoItem("1", "Test item", "needs_action", "todo.lista_zakupow")
        )

        coEvery { todoListController.getTodoLists() } returns Result.success(mockLists)
        coEvery { todoListController.getListItems(any()) } returns Result.success(mockItems)

        // When
        todoSyncService.syncLists()

        // Then
        coVerify(exactly = 1) { todoListController.getTodoLists() }
        coVerify(exactly = 2) { todoListController.getListItems(any()) }
        assertEquals(TodoSyncService.SyncState.Success, todoSyncService.syncState.value)
    }

    @Test
    fun `syncLists handles getTodoLists error`() = runTest {
        // Given
        val error = RuntimeException("Network error")
        coEvery { todoListController.getTodoLists() } returns Result.failure(error)

        // When
        todoSyncService.syncLists()

        // Then
        coVerify(exactly = 1) { todoListController.getTodoLists() }
        coVerify(exactly = 0) { todoListController.getListItems(any()) }
        assert(todoSyncService.syncState.value is TodoSyncService.SyncState.Error)
    }

    @Test
    fun `handleStateChange processes todo list changes`() = runTest {
        val eventJson = """
        {
            "event_type": "state_changed",
            "data": {
                "entity_id": "todo.lista_zakupow",
                "new_state": {
                    "attributes": {
                        "items": [
                            {
                                "uid": "1",
                                "summary": "Test item",
                                "status": "needs_action"
                            }
                        ]
                    }
                }
            }
        }
        """.trimIndent()

        val event = WebSocketMessage.Event(
            id = 1,
            event = JsonParser.parseString(eventJson).asJsonObject
        )

        todoSyncService.handleStateChange(event)
    }

    @Test
    fun `startSync schedules periodic synchronization`() = runTest {
        // Given
        val mockLists = listOf(TodoList("todo.lista_zakupow", "Lista zakupów"))
        val mockItems = listOf(
            TodoItem("1", "Test item", "needs_action", "complete", "todo.lista_zakupow")
        )

        coEvery { todoListController.getTodoLists() } returns Result.success(mockLists)
        coEvery { todoListController.getListItems(any()) } returns Result.success(mockItems)

        // When
        todoSyncService.startSync(1000) // 1 sekunda interwał
        advanceTimeBy(2500) // Przeskocz 2.5 sekundy

        // Then
        coVerify(atLeast = 2) { todoListController.getTodoLists() }
    }

    @Test
    fun `stopSync cancels periodic synchronization`() = runTest {
        // Given
        val mockLists = listOf(TodoList("todo.lista_zakupow", "Lista zakupów"))
        
        coEvery { todoListController.getTodoLists() } returns Result.success(mockLists)

        // When
        todoSyncService.startSync(1000)
        advanceTimeBy(500)
        todoSyncService.stopSync()
        advanceTimeBy(1500)

        // Then
        coVerify(atMost = 1) { todoListController.getTodoLists() }
        assertEquals(TodoSyncService.SyncState.Idle, todoSyncService.syncState.value)
    }

    @Test
    fun `syncList handles item sync failure`() = runTest {
        // Given
        val listId = "todo.lista_zakupow"
        val error = RuntimeException("Failed to sync items")

        coEvery { todoListController.getListItems(listId) } returns Result.failure(error)

        // When
        todoSyncService.syncList(listId)

        // Then
        coVerify { todoListController.getListItems(listId) }
    }
}
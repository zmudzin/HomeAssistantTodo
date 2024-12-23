package com.example.homeassistanttodo.data.local

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.homeassistanttodo.data.local.dao.TodoItemDao
import com.example.homeassistanttodo.data.local.entity.TodoItem
import com.example.homeassistanttodo.data.local.entity.TodoItemStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.Assert.assertEquals

@RunWith(AndroidJUnit4::class)
class TodoDaoTest {
    private lateinit var database: AppDatabase
    private lateinit var todoDao: TodoItemDao

    @Before
    fun createDb() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            AppDatabase::class.java
        ).build()
        todoDao = database.todoItemDao()
    }

    @After
    fun closeDb() {
        database.close()
    }

    @Test
    fun insertAndReadTodos() = runBlocking {
        val todoItem = TodoItem(
            uid = "1",
            summary = "Test Todo",
            status = TodoItemStatus.NEEDS_ACTION,
            orderPosition = 1
        )

        todoDao.insertItems(listOf(todoItem))

        val items = todoDao.getAllItems().first()
        assertEquals(1, items.size)
        assertEquals(todoItem, items[0])
    }
}
